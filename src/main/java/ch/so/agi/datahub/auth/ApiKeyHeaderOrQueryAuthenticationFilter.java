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
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyHeaderOrQueryAuthenticationFilter extends OncePerRequestFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationManager authenticationManager;

    private final String headerName;

    private final String queryParamName;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final AccessDeniedHandler accessDeniedHandler;

    public ApiKeyHeaderOrQueryAuthenticationFilter(AuthenticationManager authenticationManager, String headerName,
            String queryParamName, AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler) {
        this.authenticationManager = authenticationManager;
        this.headerName = headerName;
        this.queryParamName = queryParamName;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(headerName);
        if (!StringUtils.hasText(apiKey)) {
            apiKey = request.getParameter(queryParamName);
        }
        if (!StringUtils.hasText(apiKey)) {
            logger.warn("Did not find api key header or query parameter in request");
            authenticationEntryPoint.commence(request, response,
                    new InsufficientAuthenticationException("Did not find api key header or query parameter in request."));
            return;
        }

        try {
            Authentication authentication = this.authenticationManager
                    .authenticate(new ApiKeyHeaderAuthenticationToken(apiKey));
            if (authentication == null || !authentication.isAuthenticated()) {
                accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden."));
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden.", e));
        }
    }
}
