package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ApiKeyFormatTest {
    @Test
    void parsesValidApiKey() {
        UUID keyId = UUID.randomUUID();
        UUID secret = UUID.randomUUID();
        String apiKey = ApiKeyFormat.buildApiKey(keyId, secret);

        ApiKeyFormat.ApiKeyParts parts = ApiKeyFormat.parseApiKey(apiKey).orElseThrow();

        assertThat(parts.keyId()).isEqualTo(keyId);
        assertThat(parts.secret()).isEqualTo(secret);
    }

    @Test
    void rejectsInvalidApiKey() {
        assertThat(ApiKeyFormat.parseApiKey("not-a-key")).isEmpty();
        assertThat(ApiKeyFormat.parseApiKey("only.one.part.extra")).isEmpty();
    }

    @Test
    void parsesStoredValue() {
        UUID keyId = UUID.randomUUID();
        String stored = ApiKeyFormat.buildStoredValue(keyId, "$2a$10$hash");

        ApiKeyFormat.StoredApiKeyParts parts = ApiKeyFormat.parseStoredValue(stored).orElseThrow();

        assertThat(parts.keyId()).isEqualTo(keyId);
        assertThat(parts.hashedSecret()).isEqualTo("$2a$10$hash");
    }

    @Test
    void ignoresLegacyStoredValue() {
        assertThat(ApiKeyFormat.parseStoredValue("$2a$10$hash")).isEmpty();
    }
}
