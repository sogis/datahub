package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;

import org.apache.cayenne.ObjectContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;

class DeliveryAuthorizationFilterTest {

    @Test
    void missingParametersReturnsApiError() throws Exception {
        ObjectContext objectContext = mock(ObjectContext.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        DeliveryAuthorizationFilter filter = new DeliveryAuthorizationFilter(objectContext, mapper);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/deliveries");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        var json = mapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(json.get("message").asText()).isEqualTo("Missing parameter");
        assertThat(json.get("path").asText()).isEqualTo("/api/deliveries");
        assertThat(json.get("timestamp").asText()).isNotBlank();
    }
}
