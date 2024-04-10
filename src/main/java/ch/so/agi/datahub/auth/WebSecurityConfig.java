package ch.so.agi.datahub.auth;

import java.util.ResourceBundle;

import org.apache.cayenne.ObjectContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.datahub.service.EmailService;

@Configuration
public class WebSecurityConfig {

    @Value("${app.apiKeyHeaderName}")
    private String apiKeyHeaderName;
    
    @Autowired
    @Qualifier("customAuthenticationEntryPoint")
    AuthenticationEntryPoint authEntryPoint;

    @Autowired
    ObjectContext objectContext;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    ObjectMapper mapper;
    
    @Autowired
    EmailService emailService;
    
    @Autowired
    ResourceBundle resourceBundle;
  
    // Bean-Methode darf nicht den gleichen Namen wie die Klasse haben.
    @Bean
    FilterRegistrationBean<DeliveryAuthorizationFilter> deliveryAuthFilter(DeliveryAuthorizationFilter authorizationFilter) {
        FilterRegistrationBean<DeliveryAuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(authorizationFilter);
        //registrationBean.addUrlPatterns("/api/deliveries/**", "/und_noch_andere/*");
        //registrationBean.addUrlPatterns("*");
        registrationBean.addUrlPatterns("/api/deliveries/*");
        return registrationBean;
    }
    
    @Bean
    FilterRegistrationBean<RevokeApiKeyFilter> revokeKeyFilter() {
        FilterRegistrationBean<RevokeApiKeyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(getRevokeApiKeyFilter());
        registrationBean.addUrlPatterns("*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    RevokeApiKeyFilter getRevokeApiKeyFilter() {
        RevokeApiKeyFilter revokeApiKeyFilter = new RevokeApiKeyFilter(apiKeyHeaderName, objectContext, encoder, emailService, mapper, resourceBundle);
        return revokeApiKeyFilter;
    }
    
                
    @Autowired
    private ApiKeyHeaderAuthenticationService apiKeyHeaderAuthService;

    @Bean
    AuthenticationManager authenticationManager() {
        ApiKeyHeaderAuthenticationProvider apiKeyHeaderAuthenticationProvider = new ApiKeyHeaderAuthenticationProvider(apiKeyHeaderAuthService);
        return new ProviderManager(apiKeyHeaderAuthenticationProvider);
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiKeySecurityFilterChain(HttpSecurity http) throws Exception {                
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> 
                    sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // /api/jobs: Bewusster Entscheid, dass dieser Bereich unauthentifiziert sein soll.
                .securityMatcher("/api/keys/**", "/api/deliveries/**", "/protected/**")
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> registry
                        // permitAll() isn't the same as no security and skip all filters
                        // D.h. der ApiKeyHeaderAuthenticationFilter wird trotzdem ausgeführt.
                        // Wenn man das nicht will, muss man mit securityMatcher feingranularer definieren
                        // was wie geschützt sein soll. Hilft auch, wenn man eine zweite 
                        // SecurityFilterChain hat.
                        //.requestMatchers(AntPathRequestMatcher.antMatcher("/public/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new ApiKeyHeaderAuthenticationFilter(authenticationManager(), apiKeyHeaderName), LogoutFilter.class)
                // TODO
//                .exceptionHandling(exceptionHandling ->
//                    exceptionHandling.authenticationEntryPoint(authEntryPoint)
//                )
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }    
}
