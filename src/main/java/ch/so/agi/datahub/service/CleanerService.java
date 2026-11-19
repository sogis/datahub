package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import java.io.IOException;
import java.nio.file.*;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@ConditionalOnProperty(name = "app.cleanerEnabled", havingValue = "true")
public class CleanerService {
  private static final Logger log = LoggerFactory.getLogger(CleanerService.class);
  private final JdbcTemplate jdbc;
  private final TransactionTemplate transactions;
  private final DeliveryFiles files;
  private final AppProperties properties;
  private final Clock clock;

  public CleanerService(JdbcTemplate jdbc, TransactionTemplate transactions, DeliveryFiles files,
      AppProperties properties, Clock clock) {
    this.jdbc = jdbc;
    this.transactions = transactions;
    this.files = files;
    this.properties = properties;
    this.clock = clock;
  }

  @Scheduled(fixedDelay = 3600000)
  public void cleanUp() {
    var table = properties.logSchema() + ".deliveries_delivery";
    var jobs = properties.jobSchema() + ".jobrunr_jobs";
    var eligible = """
        FROM %s d LEFT JOIN %s j ON j.id=d.jobid::char(36)
        WHERE d.artifacts_expired IS NOT TRUE
          AND (j.state IS NULL OR j.state IN ('SUCCEEDED','FAILED','DELETED'))
          AND COALESCE(d.finished_at,
                CASE WHEN j.state IN ('FAILED','DELETED')
                  THEN timezone(?, j.updatedat AT TIME ZONE 'UTC') END) < ?
        """.formatted(table, jobs);
    var zone = clock.getZone().getId();
    var cutoff = properties.artifactCutoff(clock);
    for (String id : jdbc.queryForList("SELECT d.jobid " + eligible, String.class, zone, cutoff)) {
      try {
        transactions.executeWithoutResult(status -> {
          // A retry holding this lock owns the input. Recheck eligibility after the initial scan.
          if (jdbc.queryForList("SELECT d.jobid " + eligible + " AND d.jobid=? FOR UPDATE OF d SKIP LOCKED",
              String.class, zone, cutoff, id).isEmpty()) return;
          try {
            files.delete(UUID.fromString(id));
          } catch (IOException | IllegalArgumentException e) {
            log.warn("Could not clean up artifacts for delivery {}", id, e);
            return;
          }
          jdbc.update("""
              UPDATE %s SET artifacts_expired=true,
                processing_status=CASE processing_status
                  WHEN 'PENDING' THEN 'FAILED'
                  WHEN 'PUBLISHING' THEN 'PUBLICATION_FAILED'
                  ELSE processing_status END
              WHERE jobid=?
              """.formatted(table), id);
        });
      } catch (org.springframework.dao.DataAccessException e) {
        log.warn("Could not record cleanup for delivery {}", id, e);
      }
    }
    cleanOrphans(table);
  }

  private void cleanOrphans(String table) {
    Path root = Path.of(properties.getWorkDirectory());
    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) return;
    var cutoff = Instant.now(clock).minus(properties.getArtifactRetention());
    String prefix = properties.getFolderPrefix();
    try (var folders = Files.list(root)) {
      for (var folder : folders.toList()) {
        String name = folder.getFileName().toString();
        if (!name.startsWith(prefix) || !Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS))
          continue;
        UUID id;
        try {
          id = UUID.fromString(name.substring(prefix.length()));
        } catch (IllegalArgumentException e) {
          continue;
        }
        if (!name.equals(prefix + id)) continue;
        try {
          if (!Files.getLastModifiedTime(folder, LinkOption.NOFOLLOW_LINKS)
              .toInstant().isBefore(cutoff)) continue;
          // Keep directories whose upload or log was still being written recently.
          boolean recent = false;
          try (var contents = Files.list(folder)) {
            for (var entry : contents.toList()) {
              if (!Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS)
                  .toInstant().isBefore(cutoff)) {
                recent = true;
                break;
              }
            }
          }
          if (recent) continue;
          Boolean registered = jdbc.queryForObject(
              "SELECT EXISTS (SELECT 1 FROM " + table + " WHERE jobid=?)",
              Boolean.class, id.toString());
          if (!Boolean.TRUE.equals(registered)) files.delete(id);
        } catch (IOException e) {
          log.warn("Could not inspect or clean orphan upload {}", id, e);
        }
      }
    } catch (IOException e) {
      log.warn("Could not inspect orphan upload directories", e);
    }
  }
}
