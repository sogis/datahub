package ch.so.agi.datahub.auth;

import static org.assertj.core.api.Assertions.*;

import ch.so.agi.datahub.config.AppProperties;
import java.time.Clock;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ApiKeyHeaderAuthenticationServiceTest {
  @Test
  void authenticatesStableIdentityAndErasesSecret() {
    var encoder = new BCryptPasswordEncoder();
    var id = UUID.randomUUID();
    var secret = UUID.randomUUID();
    var service =
        new ApiKeyHeaderAuthenticationService(
            null, new AppProperties(), encoder, Clock.systemUTC()) {
          @Override
          protected Optional<ActiveKey> loadActiveApiKeyById(UUID requested) {
            assertThat(requested).isEqualTo(id);
            return Optional.of(
                new ActiveKey(
                    ApiKeyFormat.buildStoredValue(id, encoder.encode(secret.toString())),
                    42,
                    "Acme",
                    "USER"));
          }
        };
    var token = service.authenticate(ApiKeyFormat.buildApiKey(id, secret));
    assertThat(token.isAuthenticated()).isTrue();
    assertThat(token.getCredentials()).isNull();
    var actor = (ApiKeyUser) token.getPrincipal();
    assertThat(actor.organisationId()).isEqualTo(42);
    assertThat(actor.keyId()).isEqualTo(id);
    assertThat(token.getName()).isEqualTo("Acme");
    assertThatThrownBy(() -> service.authenticate(ApiKeyFormat.buildApiKey(id, UUID.randomUUID())))
        .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
  }
}
