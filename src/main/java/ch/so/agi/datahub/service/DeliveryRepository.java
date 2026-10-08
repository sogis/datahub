package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

@Repository
public class DeliveryRepository {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;
  private final AppProperties p;
  private final Clock clock;

  public JdbcTemplate jdbc() {
    return jdbc;
  }

  public TransactionTemplate tx() {
    return tx;
  }

  public AppProperties p() {
    return p;
  }

  public Clock clock() {
    return clock;
  }

  public DeliveryRepository(
      JdbcTemplate jdbc, TransactionTemplate tx, AppProperties p, Clock clock) {
    this.jdbc = jdbc;
    this.tx = tx;
    this.p = p;
    this.clock = clock;
  }

  String table(String name) {
    return p.logSchema() + "." + name;
  }

  LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  public java.util.List<Map<String, Object>> deliveryForAttempt(String id) {
    return jdbc.queryForList(
        "SELECT d.jobid FROM "
            + table("deliveries_delivery")
            + " d JOIN "
            + table("validation_attempt")
            + " a ON a.delivery_id=d.t_id WHERE a.id=?",
        id);
  }

  public Map<String, Object> find(String id) {
    return jdbc
        .queryForList("SELECT * FROM " + table("deliveries_delivery") + " WHERE jobid=?", id)
        .stream()
        .findFirst()
        .orElse(null);
  }

  public Map<String, Object> attempt(String delivery, String attempt) {
    return jdbc
        .queryForList(
            "SELECT d.*,a.version,a.state AS attempt_state,a.validator_version,a.result_models FROM"
                + " "
                + table("deliveries_delivery")
                + " d JOIN "
                + table("validation_attempt")
                + " a ON a.delivery_id=d.t_id WHERE d.jobid=? AND a.id=? AND d.attempt_id=a.id",
            delivery,
            attempt)
        .stream()
        .findFirst()
        .orElse(null);
  }

  public Optional<Long> claim(String delivery, String attempt) {
    return tx.execute(
        status -> {
          var rows =
              jdbc.queryForList(
                  "UPDATE "
                      + table("validation_attempt")
                      + " a SET state='PROCESSING',version=version+1,started_at=?,heartbeat_at=?"
                      + " FROM "
                      + table("deliveries_delivery")
                      + " d WHERE a.delivery_id=d.t_id AND d.jobid=? AND d.attempt_id=a.id AND"
                      + " a.id=? AND a.state='ENQUEUED' RETURNING a.version",
                  now(),
                  now(),
                  delivery,
                  attempt);
          if (rows.isEmpty()) return Optional.empty();
          jdbc.update(
              "UPDATE "
                  + table("deliveries_delivery")
                  + " SET execution_status='PROCESSING',updated_at=? WHERE jobid=?",
              now(),
              delivery);
          return Optional.of(((Number) rows.getFirst().get("version")).longValue());
        });
  }

  public void heartbeat(String attempt, long version) {
    jdbc.update(
        "UPDATE "
            + table("validation_attempt")
            + " SET heartbeat_at=? WHERE id=? AND version=? AND state='PROCESSING'",
        now(),
        attempt,
        version);
  }

  public boolean finish(
      String delivery,
      String attempt,
      long version,
      Boolean valid,
      String models,
      String validator) {
    return Boolean.TRUE.equals(
        tx.execute(
            status -> {
              String state = valid == null ? "FAILED" : "SUCCEEDED";
              if (jdbc.update(
                      "UPDATE "
                          + table("validation_attempt")
                          + " SET state=?,finished_at=?,result_models=?,validator_version=? WHERE"
                          + " id=? AND version=? AND state='PROCESSING'",
                      state,
                      now(),
                      models,
                      validator,
                      attempt,
                      version)
                  != 1) return false;
              jdbc.update(
                  "UPDATE "
                      + table("deliveries_delivery")
                      + " SET execution_status=?,validation_status=?,isvalid=?,isdelivered=false,publication_status=?,finished_at=?,updated_at=?"
                      + " WHERE jobid=? AND attempt_id=?",
                  state,
                  valid == null ? null : valid ? "DONE" : "FAILED",
                  valid,
                  Boolean.TRUE.equals(valid) ? "PENDING" : "NOT_APPLICABLE",
                  now(),
                  now(),
                  delivery,
                  attempt);
              return true;
            }));
  }

  public void recordProvenance(
      String attempt, long version, ValidationOutcome outcome, String transferVersion) {
    jdbc.update(
        "UPDATE "
            + table("validation_attempt")
            + " SET model_metadata=?,configuration_snapshot=?,transfer_version=? WHERE id=? AND"
            + " version=? AND state='PROCESSING'",
        outcome == null ? null : outcome.modelMetadata(),
        outcome == null ? null : outcome.configurationSnapshot(),
        transferVersion,
        attempt,
        version);
  }

  public void expireClaims() {
    tx.executeWithoutResult(
        status -> {
          var rows =
              jdbc.queryForList(
                  "UPDATE "
                      + table("validation_attempt")
                      + " SET state='FAILED',version=version+1,finished_at=? WHERE"
                      + " state='PROCESSING' AND heartbeat_at<? RETURNING delivery_id,id",
                  now(),
                  now().minus(p.getHeartbeatTimeout()));
          for (var row : rows)
            jdbc.update(
                "UPDATE "
                    + table("deliveries_delivery")
                    + " SET execution_status='FAILED',publication_status='NOT_APPLICABLE',finished_at=?,updated_at=?"
                    + " WHERE t_id=? AND attempt_id=?",
                now(),
                now(),
                row.get("delivery_id"),
                row.get("id"));
        });
  }

  public static String string(Map<String, Object> row, String key) {
    return (String) row.get(key);
  }

  public static long number(Map<String, Object> row, String key) {
    return ((Number) row.get(key)).longValue();
  }

  public static LocalDateTime time(Map<String, Object> row, String key) {
    Object v = row.get(key);
    return v == null ? null : v instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime) v;
  }
}
