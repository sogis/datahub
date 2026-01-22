package ch.so.agi.datahub.auth;

import java.util.Optional;
import java.util.UUID;

public final class ApiKeyFormat {
    private static final String KEY_SEPARATOR = ".";
    private static final String STORED_SEPARATOR = ":";

    private ApiKeyFormat() {
    }

    public static String buildApiKey(UUID keyId, UUID secret) {
        return keyId + KEY_SEPARATOR + secret;
    }

    public static String buildStoredValue(UUID keyId, String hashedSecret) {
        return keyId + STORED_SEPARATOR + hashedSecret;
    }

    public static Optional<ApiKeyParts> parseApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        String[] parts = apiKey.split("\\.", -1);
        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            UUID keyId = UUID.fromString(parts[0]);
            UUID secret = UUID.fromString(parts[1]);
            return Optional.of(new ApiKeyParts(keyId, secret));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static Optional<StoredApiKeyParts> parseStoredValue(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return Optional.empty();
        }

        int separatorIndex = storedValue.indexOf(STORED_SEPARATOR);
        if (separatorIndex <= 0 || separatorIndex == storedValue.length() - 1) {
            return Optional.empty();
        }

        String keyIdPart = storedValue.substring(0, separatorIndex);
        String hashPart = storedValue.substring(separatorIndex + 1);

        try {
            UUID keyId = UUID.fromString(keyIdPart);
            return Optional.of(new StoredApiKeyParts(keyId, hashPart));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record ApiKeyParts(UUID keyId, UUID secret) {
    }

    public record StoredApiKeyParts(UUID keyId, String hashedSecret) {
    }
}
