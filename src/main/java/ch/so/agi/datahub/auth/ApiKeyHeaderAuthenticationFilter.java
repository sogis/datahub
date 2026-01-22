package ch.so.agi.datahub.auth;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyHeaderAuthenticationFilter extends OncePerRequestFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationManager authenticationManager;
    
    private final String headerName;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final AccessDeniedHandler accessDeniedHandler;

    public ApiKeyHeaderAuthenticationFilter(AuthenticationManager authenticationManager, final String headerName,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler) {
        this.authenticationManager = authenticationManager;
        this.headerName = headerName;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String apiKey = request.getHeader(headerName);
        if(apiKey == null) {
            logger.warn("Did not find api key header in request");
            authenticationEntryPoint.commence(request, response,
                    new InsufficientAuthenticationException("Did not find api key header in request."));
            return;
        }
        
        try {
            Authentication authentication = this.authenticationManager.authenticate(new ApiKeyHeaderAuthenticationToken(apiKey));
            if (authentication == null || !authentication.isAuthenticated()) {
                accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden."));
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);                

            // Falls es eine zweite Security Chain gibt, die den gleichen
            // Security Matcher hat, wird der unauthentifizerte Request in der
            // zweiten Chain auch behandelt und z.B. auf eine Login-Seite verwiesen.
            // Das wäre dann wahrscheinlich nicht gewünscht. Man müsste hier bereits
            // abbrechen: "if/else isAuthenticated".
        } catch (AuthenticationException e) {
            accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden.", e));
        }
    }
}
