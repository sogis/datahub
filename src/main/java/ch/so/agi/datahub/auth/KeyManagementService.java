package ch.so.agi.datahub.auth;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.config.AppProperties;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KeyManagementService {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;
  private final PasswordEncoder encoder;
  private final ch.so.agi.datahub.service.EmailService mail;
  private final AppProperties p;
  private final Clock clock;

  public KeyManagementService(
      JdbcTemplate jdbc,
      TransactionTemplate tx,
      PasswordEncoder encoder,
      ch.so.agi.datahub.service.EmailService mail,
      AppProperties p,
      Clock clock) {
    this.jdbc = jdbc;
    this.tx = tx;
    this.encoder = encoder;
    this.mail = mail;
    this.p = p;
    this.clock = clock;
  }

  public void issue(Authentication authentication, String organisation) {
    if (!p.isMailEnabled())
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Key issuance requires email delivery.");
    var actor = (ApiKeyUser) authentication.getPrincipal();
    boolean admin =
        authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(AppConstants.ROLE_NAME_ADMIN));
    if (admin && (organisation == null || organisation.isBlank()))
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Parameter 'organisation' is required.");
    var rows =
        jdbc.queryForList(
            "SELECT t_id,email FROM "
                + p.configSchema()
                + ".core_organisation WHERE "
                + (admin ? "aname=?" : "t_id=?"),
            admin ? organisation : actor.getOrganisationId());
    if (rows.size() != 1)
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Object not found.");
    var id = UUID.randomUUID();
    var secret = UUID.randomUUID();
    String stored = ApiKeyFormat.buildStoredValue(id, encoder.encode(secret.toString()));
    jdbc.update(
        "INSERT INTO "
            + p.configSchema()
            + ".core_apikey(apikey,createdat,organisation_r,issuance_state)"
            + " VALUES(?,?,?,'PENDING')",
        stored,
        now(),
        rows.getFirst().get("t_id"));
    try {
      mail.send(
          (String) rows.getFirst().get("email"),
          "Neuer Datahub API-Key",
          ApiKeyFormat.buildApiKey(id, secret));
      if (jdbc.update(
              "UPDATE "
                  + p.configSchema()
                  + ".core_apikey SET issuance_state='ACTIVE' WHERE apikey=? AND"
                  + " issuance_state='PENDING'",
              stored)
          != 1) throw new IllegalStateException("Key activation failed");
    } catch (Exception e) {
      jdbc.update(
          "UPDATE "
              + p.configSchema()
              + ".core_apikey SET issuance_state='FAILED' WHERE apikey=? AND"
              + " issuance_state='PENDING'",
          stored);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Key could not be delivered and activated.");
    }
  }

  public boolean revoke(Authentication authentication, String fullKey) {
    var actor = (ApiKeyUser) authentication.getPrincipal();
    return revoke(fullKey, actor.getOrganisationId(), false);
  }

  public boolean revokeInsecure(String fullKey) {
    return revoke(fullKey, null, true);
  }

  private boolean revoke(String fullKey, Long organisationId, boolean notify) {
    var parts = ApiKeyFormat.parseApiKey(fullKey);
    if (parts.isEmpty()) return false;
    return Boolean.TRUE.equals(
        tx.execute(
            status -> {
              var rows =
                  jdbc.queryForList(
                      "SELECT k.t_id,k.apikey,k.organisation_r,o.email FROM "
                          + p.configSchema()
                          + ".core_apikey k JOIN "
                          + p.configSchema()
                          + ".core_organisation o ON o.t_id=k.organisation_r WHERE k.apikey LIKE ?"
                          + " AND k.revokedat IS NULL FOR UPDATE OF k",
                      parts.get().keyId() + ":%");
              if (rows.size() != 1) return false;
              var row = rows.getFirst();
              var stored = ApiKeyFormat.parseStoredValue((String) row.get("apikey"));
              if (stored.isEmpty()
                  || !stored.get().keyId().equals(parts.get().keyId())
                  || !encoder.matches(parts.get().secret().toString(), stored.get().hashedSecret()))
                return false;
              if (organisationId != null
                  && ((Number) row.get("organisation_r")).longValue() != organisationId)
                return false;
              jdbc.update(
                  "UPDATE " + p.configSchema() + ".core_apikey SET revokedat=? WHERE t_id=?",
                  now(),
                  row.get("t_id"));
              if (notify && p.isMailEnabled())
                jdbc.update(
                    "INSERT INTO "
                        + p.logSchema()
                        + ".notification(id,recipient,subject,body,state,tries,next_at)"
                        + " VALUES(?,?,?,?, 'PENDING',0,?)",
                    UUID.randomUUID().toString(),
                    row.get("email"),
                    "Datahub API-Key widerrufen",
                    "Ein API-Key wurde wegen einer ungeschützten HTTP-Anfrage widerrufen.",
                    now());
              return true;
            }));
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }
}
