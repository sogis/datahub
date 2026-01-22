package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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

        ApiKeyHeaderAuthenticationService service = new TestAuthService(encoder, Optional.of(apiKey), List.of());

        ApiKeyHeaderAuthenticationToken token = service.authenticate(
                new ApiKeyHeaderAuthenticationToken(ApiKeyFormat.buildApiKey(keyId, secret)));

        assertThat(token.isAuthenticated()).isTrue();
        assertThat(token.getName()).isEqualTo("Acme");
    }

    @Test
    void authenticatesLegacyKeyWithoutKeyId() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String legacyKey = UUID.randomUUID().toString();

        CoreOrganisation organisation = new CoreOrganisation();
        organisation.setAname("Legacy");
        organisation.setArole("ROLE_USER");

        CoreApikey apiKey = new CoreApikey();
        apiKey.setApikey(encoder.encode(legacyKey));
        apiKey.writePropertyDirectly("coreOrganisation", organisation);

        ApiKeyHeaderAuthenticationService service = new TestAuthService(encoder, Optional.empty(), List.of(apiKey));

        ApiKeyHeaderAuthenticationToken token = service.authenticate(
                new ApiKeyHeaderAuthenticationToken(legacyKey));

        assertThat(token.isAuthenticated()).isTrue();
        assertThat(token.getName()).isEqualTo("Legacy");
    }

    private static class TestAuthService extends ApiKeyHeaderAuthenticationService {
        private final Optional<CoreApikey> apiKeyById;
        private final List<CoreApikey> apiKeys;

        TestAuthService(PasswordEncoder encoder, Optional<CoreApikey> apiKeyById, List<CoreApikey> apiKeys) {
            super(null, encoder);
            this.apiKeyById = apiKeyById;
            this.apiKeys = apiKeys;
        }

        @Override
        protected Optional<CoreApikey> loadActiveApiKeyById(UUID keyId) {
            return apiKeyById;
        }

        @Override
        protected List<CoreApikey> loadActiveApiKeys() {
            return apiKeys;
        }
    }
}
