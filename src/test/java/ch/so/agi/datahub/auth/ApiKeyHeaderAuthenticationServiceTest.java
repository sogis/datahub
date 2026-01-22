package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ch.so.agi.datahub.cayenne.CoreApikey;
import ch.so.agi.datahub.cayenne.CoreOrganisation;

class ApiKeyHeaderAuthenticationServiceTest {
    @Test
    void authenticatesWithKeyIdLookup() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        UUID keyId = UUID.randomUUID();
        UUID secret = UUID.randomUUID();
        String storedValue = ApiKeyFormat.buildStoredValue(keyId, encoder.encode(secret.toString()));

        CoreOrganisation organisation = new CoreOrganisation();
        organisation.setAname("Acme");
        organisation.setArole("ROLE_USER");

        CoreApikey apiKey = new CoreApikey();
        apiKey.setApikey(storedValue);
        apiKey.writePropertyDirectly("coreOrganisation", organisation);

        ApiKeyHeaderAuthenticationService service = new TestAuthService(encoder, Optional.of(apiKey));

        ApiKeyHeaderAuthenticationToken token = service.authenticate(
                new ApiKeyHeaderAuthenticationToken(ApiKeyFormat.buildApiKey(keyId, secret)));

        assertThat(token.isAuthenticated()).isTrue();
        assertThat(token.getName()).isEqualTo("Acme");
    }

    private static class TestAuthService extends ApiKeyHeaderAuthenticationService {
        private final Optional<CoreApikey> apiKeyById;

        TestAuthService(PasswordEncoder encoder, Optional<CoreApikey> apiKeyById) {
            super(null, encoder);
            this.apiKeyById = apiKeyById;
        }

        @Override
        protected Optional<CoreApikey> loadActiveApiKeyById(UUID keyId) {
            return apiKeyById;
        }

    }
}
