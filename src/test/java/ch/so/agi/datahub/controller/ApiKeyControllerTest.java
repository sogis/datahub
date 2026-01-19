package ch.so.agi.datahub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.cayenne.ObjectContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.model.ApiError;
import ch.so.agi.datahub.service.EmailService;

class ApiKeyControllerTest {

    @Test
    void createApiKeyMissingOrganisationReturnsApiError() {
        ObjectContext objectContext = mock(ObjectContext.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        EmailService emailService = mock(EmailService.class);
        ResourceBundle resourceBundle = mock(ResourceBundle.class);

        ApiKeyController controller = new ApiKeyController(objectContext, encoder, emailService, resourceBundle);

        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(AppConstants.ROLE_NAME_ADMIN));
        doReturn(authorities).when(authentication).getAuthorities();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/keys");
        ResponseEntity<?> response = controller.createApiKey(authentication, null, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isInstanceOf(ApiError.class);
        ApiError error = (ApiError) response.getBody();
        assertThat(error.message()).isEqualTo("Parameter 'organisation' is required.");
        assertThat(error.path()).isEqualTo("/api/keys");
        assertThat(error.timestamp()).isNotNull();
    }
}
