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

@Service
@ConditionalOnProperty(name = "app.cleanerEnabled", havingValue = "true")
public class CleanerService {
  private static final Logger log = LoggerFactory.getLogger(CleanerService.class);
  private final JdbcTemplate jdbc;
  private final DeliveryFiles files;
  private final AppProperties properties;
  private final Clock clock;

  public CleanerService(JdbcTemplate jdbc, DeliveryFiles files, AppProperties properties, Clock clock) {
    this.jdbc = jdbc;
    this.files = files;
    this.properties = properties;
    this.clock = clock;
  }

  @Scheduled(fixedDelay = 3600000)
  public void cleanUp() {
    var table = properties.logSchema() + ".deliveries_delivery";
    var jobs = properties.jobSchema() + ".jobrunr_jobs";
    for (String id : jdbc.queryForList("SELECT d.jobid FROM " + table
        + " d WHERE d.finished_at<? AND d.artifacts_expired IS NOT TRUE"
        + " AND d.processing_status IN ('INVALID','SUPERSEDED','DELIVERED','FAILED','PUBLICATION_FAILED')"
        + " AND NOT EXISTS (SELECT 1 FROM " + jobs + " j WHERE j.id=d.jobid::char(36)"
        + " AND j.state NOT IN ('SUCCEEDED','FAILED','DELETED'))",
        String.class, properties.artifactCutoff(clock))) {
      try {
        files.delete(UUID.fromString(id));
        jdbc.update("UPDATE " + table + " SET artifacts_expired=true WHERE jobid=?", id);
      } catch (IOException | IllegalArgumentException e) {
        log.warn("Could not clean up artifacts for delivery {}", id, e);
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
