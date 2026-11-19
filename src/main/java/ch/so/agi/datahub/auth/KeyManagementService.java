package ch.so.agi.datahub.auth;

import ch.so.agi.datahub.cayenne.CoreApikey;
import ch.so.agi.datahub.cayenne.CoreOrganisation;
import ch.so.agi.datahub.config.AppProperties;
import org.apache.cayenne.CayenneRuntime;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.cayenne.query.ObjectSelect;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KeyManagementService {
  private final CayenneRuntime cayenne;
  private final JdbcTemplate jdbc;
  private final PasswordEncoder encoder;
  private final AppProperties properties;
  private final Clock clock;

  public KeyManagementService(CayenneRuntime cayenne, JdbcTemplate jdbc, PasswordEncoder encoder,
      AppProperties properties, Clock clock) {
    this.cayenne = cayenne;
    this.jdbc = jdbc;
    this.encoder = encoder;
    this.properties = properties;
    this.clock = clock;
  }

  public IssuedKey issue(String organisation) {
    if (organisation == null || organisation.isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organisation is required.");
    var context = cayenne.newContext();
    var owner = ObjectSelect.query(CoreOrganisation.class)
        .where(CoreOrganisation.ANAME.eq(organisation)).selectOne(context);
    if (owner == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found.");
    UUID id = UUID.randomUUID();
    UUID secret = UUID.randomUUID();
    var key = context.newObject(CoreApikey.class);
    key.setApikey(ApiKeyFormat.buildStoredValue(id, encoder.encode(secret.toString())));
    key.setCreatedat(LocalDateTime.now(clock));
    key.setOrganisationR(owner);
    context.commitChanges();
    return new IssuedKey(id, ApiKeyFormat.buildApiKey(id, secret));
  }

  public boolean revoke(UUID keyId) {
    return jdbc.update("UPDATE " + properties.configSchema()
        + ".core_apikey SET revokedat=? WHERE apikey LIKE ? AND revokedat IS NULL",
        LocalDateTime.now(clock), keyId + ":%") != 0;
  }

  public record IssuedKey(UUID id, String apiKey) {
    @Override
    public String toString() {
      return "IssuedKey[id=" + id + "]";
    }
  }
}
