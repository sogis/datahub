package ch.so.agi.datahub.auth;

import ch.so.agi.datahub.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@ConditionalOnWebApplication
public class WebSecurityConfig {
  @Bean
  AuthenticationManager authenticationManager(ApiKeyHeaderAuthenticationService service) {
    return new ProviderManager(new ApiKeyHeaderAuthenticationProvider(service));
  }

  @Bean
  FilterRegistrationBean<RevokeApiKeyFilter> revokeKeyFilter(
      KeyManagementService keys, AppProperties p, JsonMapper mapper) {
    var bean = new FilterRegistrationBean<>(new RevokeApiKeyFilter(keys, p, mapper));
    bean.addUrlPatterns("/*");
    bean.setOrder(1);
    return bean;
  }

  @Bean
  @Order(1)
  SecurityFilterChain files(
      HttpSecurity http,
      AuthenticationManager manager,
      AppProperties p,
      ApiErrorAuthenticationEntryPoint entry,
      ApiErrorAccessDeniedHandler denied)
      throws Exception {
    return http.securityMatcher("/api/files/**", "/ui/files/**")
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(a -> a.anyRequest().authenticated())
        .addFilterBefore(
            new ApiKeyHeaderOrQueryAuthenticationFilter(
                manager, p.getApiKeyHeaderName(), p.getApiKeyQueryParamName(), entry, denied),
            LogoutFilter.class)
        .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
        .build();
  }

  @Bean
  @Order(2)
  SecurityFilterChain api(
      HttpSecurity http,
      AuthenticationManager manager,
      AppProperties p,
      ApiErrorAuthenticationEntryPoint entry,
      ApiErrorAccessDeniedHandler denied)
      throws Exception {
    return http.securityMatcher("/api/keys/**", "/api/deliveries/**", "/protected/**")
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // Header-authenticated operations do not rely on ambient browser cookies.
        .csrf(c -> c.ignoringRequestMatchers("/api/keys/**", "/api/deliveries/**"))
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(a -> a.anyRequest().authenticated())
        .addFilterBefore(
            new ApiKeyHeaderAuthenticationFilter(manager, p.getApiKeyHeaderName(), entry, denied),
            LogoutFilter.class)
        .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
        .build();
  }

  @Bean
  @Order(3)
  SecurityFilterChain publicRoutes(HttpSecurity http) throws Exception {
    return http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        org.springframework.http.HttpMethod.GET,
                        "/",
                        "/jobs",
                        "/jobs/table",
                        "/web/jobs.xhtml",
                        "/web/index.xhtml",
                        "/api/jobs",
                        "/api/jobs/*",
                        "/api/logs/*",
                        "/ilidata.xml",
                        "/ilimodels.xml",
                        "/ilisite.xml",
                        "/files/*/*",
                        "/css/**",
                        "/js/**",
                        "/favicon.ico",
                        "/ping",
                        "/public/hello",
                        "/actuator/health/**")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .denyAll())
        .build();
  }
}
