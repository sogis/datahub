package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class ApiKeyHeaderOrQueryAuthenticationFilterTest {

    @Test
    void rejectsRequestWhenApiKeyMissing() throws ServletException, IOException {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        AccessDeniedHandler accessDeniedHandler = mock(AccessDeniedHandler.class);

        ApiKeyHeaderOrQueryAuthenticationFilter filter = new ApiKeyHeaderOrQueryAuthenticationFilter(
                authenticationManager, "X-API-KEY", "token", entryPoint, accessDeniedHandler);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new AssertionError("Filter chain should not be invoked without credentials.");
        };

        filter.doFilter(request, response, chain);

        verify(entryPoint).commence(any(), any(),
                any(org.springframework.security.authentication.InsufficientAuthenticationException.class));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void allowsRequestWithQueryParamApiKey() throws ServletException, IOException {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        AccessDeniedHandler accessDeniedHandler = mock(AccessDeniedHandler.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        ApiKeyHeaderOrQueryAuthenticationFilter filter = new ApiKeyHeaderOrQueryAuthenticationFilter(
                authenticationManager, "X-API-KEY", "token", entryPoint, accessDeniedHandler);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("token", "abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainInvoked.set(true);

        filter.doFilter(request, response, chain);

        assertThat(chainInvoked).isTrue();
        verifyNoInteractions(entryPoint);
        verifyNoInteractions(accessDeniedHandler);
    }

    @Test
    void deniesRequestWhenAuthenticationFails() throws ServletException, IOException {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        AuthenticationEntryPoint entryPoint = mock(AuthenticationEntryPoint.class);
        AccessDeniedHandler accessDeniedHandler = mock(AccessDeniedHandler.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        ApiKeyHeaderOrQueryAuthenticationFilter filter = new ApiKeyHeaderOrQueryAuthenticationFilter(
                authenticationManager, "X-API-KEY", "token", entryPoint, accessDeniedHandler);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("token", "abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new AssertionError("Filter chain should not be invoked on failed authentication.");
        };

        filter.doFilter(request, response, chain);

        verify(accessDeniedHandler).handle(any(), any(), any(AccessDeniedException.class));
    }
}
