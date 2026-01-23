package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import org.apache.cayenne.ObjectContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.datahub.service.EmailService;
import jakarta.servlet.FilterChain;

class RevokeApiKeyFilterTest {

    @Test
    void httpRequestRevokesAndReturnsApiError() throws Exception {
        ObjectContext objectContext = mock(ObjectContext.class);
        when(objectContext.selectOne(any())).thenReturn(null);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        EmailService emailService = mock(EmailService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ResourceBundle resourceBundle = mock(ResourceBundle.class);

        RevokeApiKeyFilter filter = new RevokeApiKeyFilter("X-API-KEY", objectContext, encoder,
                emailService, mapper, new String[] { "localhost" }, resourceBundle);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/keys/revoke");
        request.setScheme("http");
        request.setServerName("example.com");
        request.addHeader("X-API-KEY", "test-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        try {
            filter.doFilterInternal(request, response, chain);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        var json = mapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(json.get("message").asText()).isEqualTo("Possible API key was revoked.");
        assertThat(json.get("path").asText()).isEqualTo("/api/keys/revoke");
        assertThat(json.get("timestamp").asText()).isNotBlank();
    }

    @Test
    void missingApiKeyHeaderPassesThrough() throws Exception {
        ObjectContext objectContext = mock(ObjectContext.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        EmailService emailService = mock(EmailService.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ResourceBundle resourceBundle = mock(ResourceBundle.class);

        RevokeApiKeyFilter filter = new RevokeApiKeyFilter("X-API-KEY", objectContext, encoder,
                emailService, mapper, new String[] { "localhost" }, resourceBundle);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/keys/revoke");
        request.setScheme("https");
        request.setServerName("example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        try {
            filter.doFilterInternal(request, response, chain);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
