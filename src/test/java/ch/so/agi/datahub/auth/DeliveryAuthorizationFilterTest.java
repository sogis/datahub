package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.apache.cayenne.ObjectContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.cayenne.CoreOperat;
import ch.so.agi.datahub.cayenne.CoreOrganisation;
import ch.so.agi.datahub.cayenne.CoreTheme;
import ch.so.agi.datahub.model.DeliveryAuthorizationInfo;
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

    @Test
    void authorizedRequestAddsDeliveryInfo() throws Exception {
        ObjectContext objectContext = mock(ObjectContext.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        DeliveryAuthorizationFilter filter = new DeliveryAuthorizationFilter(objectContext, mapper);

        CoreOrganisation organisation = mock(CoreOrganisation.class);
        when(organisation.getAname()).thenReturn("Org");
        when(organisation.getEmail()).thenReturn("org@example.com");

        CoreTheme theme = mock(CoreTheme.class);
        when(theme.getConfig()).thenReturn("config.xml");
        when(theme.getMetaconfig()).thenReturn("meta.xml");

        CoreOperat operat = mock(CoreOperat.class);
        when(operat.getCoreOrganisation()).thenReturn(organisation);
        when(operat.getCoreTheme()).thenReturn(theme);
        when(objectContext.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(operat);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/deliveries");
        request.setParameter("theme", "theme");
        request.setParameter("operat", "operat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        var authentication = new UsernamePasswordAuthenticationToken(
                "Org", "password", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(AppConstants.ROLE_NAME_USER)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filter.doFilterInternal(request, response, chain);
        } finally {
            SecurityContextHolder.clearContext();
        }

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
        Object attribute = request.getAttribute(AppConstants.ATTRIBUTE_OPERAT_DELIVERY_INFO);
        assertThat(attribute).isInstanceOf(DeliveryAuthorizationInfo.class);
        DeliveryAuthorizationInfo info = (DeliveryAuthorizationInfo) attribute;
        assertThat(info.organisationName()).isEqualTo("Org");
        assertThat(info.email()).isEqualTo("org@example.com");
        assertThat(info.config()).isEqualTo("config.xml");
        assertThat(info.metaConfig()).isEqualTo("meta.xml");
    }

    @Test
    void unauthorizedRequestReturnsApiError() throws Exception {
        ObjectContext objectContext = mock(ObjectContext.class);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        DeliveryAuthorizationFilter filter = new DeliveryAuthorizationFilter(objectContext, mapper);

        when(objectContext.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/deliveries");
        request.setParameter("theme", "theme");
        request.setParameter("operat", "operat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        var authentication = new UsernamePasswordAuthenticationToken(
                "Org", "password", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(AppConstants.ROLE_NAME_USER)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filter.doFilterInternal(request, response, chain);
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        var json = mapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        assertThat(json.get("message").asText()).isEqualTo("User is not authorized");
        assertThat(json.get("path").asText()).isEqualTo("/api/deliveries");
        assertThat(json.get("timestamp").asText()).isNotBlank();
    }
}
