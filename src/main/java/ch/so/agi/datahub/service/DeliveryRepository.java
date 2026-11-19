package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;
import org.interlis2.validator.Main;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/** SQL for the JobRunr handoff and the locks around filesystem publication. */
@Repository
public class DeliveryRepository {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate transactions;
  private final String table;
  private final String jobs;
  private final Clock clock;

  public DeliveryRepository(JdbcTemplate jdbc, TransactionTemplate transactions,
      AppProperties properties, Clock clock) {
    this.jdbc = jdbc;
    this.transactions = transactions;
    this.table = properties.logSchema() + ".deliveries_delivery";
    this.jobs = properties.jobSchema() + ".jobrunr_jobs";
    this.clock = clock;
  }

  <T> T inTransaction(Supplier<T> action) {
    return transactions.execute(status -> action.get());
  }

  List<String> unqueued() {
    return jdbc.queryForList("SELECT d.jobid FROM " + table + " d"
        + " WHERE d.processing_status IN ('PENDING','PUBLISHING') AND NOT EXISTS"
        + " (SELECT 1 FROM " + jobs + " j WHERE j.id=d.jobid::char(36)) ORDER BY d.t_id LIMIT 50",
        String.class);
  }

  Delivery lock(String id) {
    return jdbc.query("SELECT * FROM " + table + " WHERE jobid=? AND artifacts_expired IS NOT TRUE"
        + " FOR UPDATE", (rs, index) -> new Delivery(
            rs.getLong("t_id"), id, rs.getString("processing_status"),
            rs.getString("theme"), rs.getString("operat"), rs.getString("email"),
            rs.getString("validator_config"), rs.getString("validator_meta_config"),
            rs.getString("expected_models")), id).stream().findFirst().orElse(null);
  }

  void lockTarget(Delivery delivery) {
    // Lock the filenames, since a configuration row may be replaced while old jobs still exist.
    jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtextextended("
        + "lower(normalize(?)) || '/' || lower(normalize(?)), 0))",
        delivery.theme(), delivery.operat());
  }

  boolean hasNewerPublication(Delivery delivery) {
    return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM " + table
        + " WHERE t_id>? AND isvalid AND lower(normalize(theme))=lower(normalize(?))"
        + " AND lower(normalize(operat))=lower(normalize(?)))",
        Boolean.class, delivery.sequence(), delivery.theme(), delivery.operat()));
  }

  void validationResult(String id, boolean valid, IliValidatorService.Transfer transfer) {
    var now = LocalDateTime.now(clock);
    jdbc.update("UPDATE " + table + " SET isvalid=?, isdelivered=?, processing_status=?,"
            + " result_models=?, transfer_version=?, validator_version=?, updated_at=?, finished_at=? WHERE jobid=?",
        valid, valid ? null : false, valid ? "PUBLISHING" : "INVALID", String.join(";", transfer.models()), transfer.version(), Main.getVersion(),
        now, valid ? null : now, id);
  }

  void complete(String id, String outcome) {
    var now = LocalDateTime.now(clock);
    jdbc.update("UPDATE " + table + " SET processing_status=?, isdelivered=?,"
        + " updated_at=?, finished_at=? WHERE jobid=?",
        outcome, "DELIVERED".equals(outcome), now, now, id);
  }

  void notificationResult(String id, String outcome) {
    jdbc.update("UPDATE " + table + " SET notification_status=? WHERE jobid=?", outcome, id);
  }

  record Delivery(long sequence, String id, String status, String theme, String operat,
      String email, String config, String metaConfig, String expectedModels) {
    boolean needsValidation() { return "PENDING".equals(status) || "FAILED".equals(status); }
    boolean needsPublication() {
      return "PUBLISHING".equals(status) || "PUBLICATION_FAILED".equals(status);
    }
  }
}
