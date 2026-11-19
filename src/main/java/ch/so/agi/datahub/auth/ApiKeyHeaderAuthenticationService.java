package ch.so.agi.datahub.auth;

import ch.so.agi.datahub.config.AppProperties;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyHeaderAuthenticationService {
  public record ActiveKey(
      String storedValue, long organisationId, String organisation, String role) {}

  private final JdbcTemplate jdbc;
  private final AppProperties properties;
  private final PasswordEncoder encoder;
  private final Clock clock;

  public ApiKeyHeaderAuthenticationService(
      JdbcTemplate jdbc, AppProperties properties, PasswordEncoder encoder, Clock clock) {
    this.jdbc = jdbc;
    this.properties = properties;
    this.encoder = encoder;
    this.clock = clock;
  }

  public UsernamePasswordAuthenticationToken authenticate(String apiKey) {
    var parts = ApiKeyFormat.parseApiKey(apiKey);
    if (parts.isEmpty()) throw new BadCredentialsException("Invalid API key");
    var key = loadActiveApiKeyById(parts.get().keyId());
    if (key.isEmpty()) throw new BadCredentialsException("Invalid API key");
    var stored = ApiKeyFormat.parseStoredValue(key.get().storedValue());
    if (stored.isEmpty()
        || !stored.get().keyId().equals(parts.get().keyId())
        || !encoder.matches(parts.get().secret().toString(), stored.get().hashedSecret()))
      throw new BadCredentialsException("Invalid API key");
    var principal =
        new ApiKeyUser(key.get().organisationId(), parts.get().keyId(), key.get().organisation());
    return UsernamePasswordAuthenticationToken.authenticated(
        principal, null, List.of(new SimpleGrantedAuthority(key.get().role())));
  }

  protected Optional<ActiveKey> loadActiveApiKeyById(UUID id) {
    // Read directly: authorization must also observe edits made by administrators outside Cayenne.
    String schema = properties.configSchema();
    return jdbc
        .query(
            "SELECT k.apikey,o.t_id,o.aname,o.arole FROM "
                + schema
                + ".core_apikey k JOIN "
                + schema
                + ".core_organisation o ON o.t_id=k.organisation_r WHERE k.apikey LIKE ? AND"
                + " k.revokedat IS NULL AND (k.dateofexpiry IS NULL"
                + " OR k.dateofexpiry>?)",
            (rs, n) ->
                new ActiveKey(rs.getString(1), rs.getLong(2), rs.getString(3), rs.getString(4)),
            id + ":%",
            LocalDateTime.now(clock))
        .stream()
        .findFirst();
  }
}
