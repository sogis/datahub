package ch.so.agi.datahub.auth;

import static ch.so.agi.datahub.AppConstants.ROLE_NAME_ADMIN;
import static ch.so.agi.datahub.AppConstants.ROLE_NAME_USER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import ch.so.agi.datahub.config.AppProperties;
import jakarta.servlet.DispatcherType;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
public class WebSecurityConfig {
  @Bean
  AuthenticationManager apiKeys(ApiKeyHeaderAuthenticationService keys) {
    return auth -> keys.authenticate((String) auth.getCredentials());
  }

  @Bean
  @Order(1)
  SecurityFilterChain fileBrowser(HttpSecurity http, ApiKeyHeaderAuthenticationService keys)
      throws Exception {
    return stateless(http.securityMatcher("/ui/files/**"))
        .authenticationManager(auth -> keys.authenticate(auth.getName() + "." + auth.getCredentials()))
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(a -> a.requestMatchers(GET, "/ui/files", "/ui/files/download")
            .hasAuthority(ROLE_NAME_ADMIN).anyRequest().denyAll())
        .build();
  }

  @Bean
  SecurityFilterChain routes(HttpSecurity http, AuthenticationManager apiKeys,
      AppProperties properties) throws Exception {
    var entry = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    var filter = new AuthenticationFilter(apiKeys,
        request -> {
          var headers = Collections.list(request.getHeaders(properties.getApiKeyHeaderName()));
          if (headers.isEmpty()) return null;
          if (headers.size() != 1) throw new BadCredentialsException("Expected one API key header");
          return UsernamePasswordAuthenticationToken.unauthenticated("api-key", headers.getFirst());
        });
    var paths = PathPatternRequestMatcher.withDefaults();
    filter.setRequestMatcher(new OrRequestMatcher(
        paths.matcher(POST, "/api/keys"), paths.matcher(DELETE, "/api/keys/*"),
        paths.matcher(POST, "/api/deliveries"), paths.matcher(GET, "/api/files"),
        paths.matcher(GET, "/protected/hello")));
    filter.setSuccessHandler((request, response, authentication) -> {});
    filter.setFailureHandler(new AuthenticationEntryPointFailureHandler(entry));
    return stateless(http)
        // These routes accept an explicit API-key header and never authenticate browser sessions.
        .csrf(c -> c.ignoringRequestMatchers("/api/keys", "/api/keys/*", "/api/deliveries"))
        .exceptionHandling(e -> e.authenticationEntryPoint(entry))
        .addFilterBefore(filter, LogoutFilter.class)
        .authorizeHttpRequests(a -> a
            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
            .requestMatchers(POST, "/api/keys")
                .hasAuthority(ROLE_NAME_ADMIN)
            .requestMatchers(DELETE, "/api/keys/*")
                .hasAuthority(ROLE_NAME_ADMIN)
            .requestMatchers(POST, "/api/deliveries").hasAnyAuthority(ROLE_NAME_USER, ROLE_NAME_ADMIN)
            .requestMatchers(GET, "/api/files").hasAuthority(ROLE_NAME_ADMIN)
            .requestMatchers(GET, "/protected/hello").authenticated()
            .requestMatchers(GET, "/", "/jobs", "/jobs/table", "/web/jobs.xhtml", "/web/index.xhtml",
                "/api/jobs", "/api/jobs/*", "/api/logs/*", "/ilidata.xml", "/ilimodels.xml",
                "/ilisite.xml", "/files/*/*", "/css/**", "/js/**", "/favicon.ico", "/ping",
                "/public/hello", "/actuator/health/**").permitAll()
            .anyRequest().denyAll())
        .build();
  }

  private HttpSecurity stateless(HttpSecurity http) throws Exception {
    return http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable);
  }
}
