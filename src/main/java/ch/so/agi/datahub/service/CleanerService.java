package ch.so.agi.datahub.service;

import ch.so.agi.datahub.config.AppProperties;
import java.io.IOException;
import java.time.Clock;
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
    for (String id : jdbc.queryForList("SELECT jobid FROM " + table
        + " WHERE finished_at<? AND artifacts_expired IS NOT TRUE"
        + " AND processing_status IN ('INVALID','SUPERSEDED','DELIVERED')",
        String.class, properties.artifactCutoff(clock))) {
      try {
        files.delete(UUID.fromString(id));
        jdbc.update("UPDATE " + table + " SET artifacts_expired=true WHERE jobid=?", id);
      } catch (IOException | IllegalArgumentException e) {
        log.warn("Could not clean up artifacts for delivery {}", id, e);
      }
    }
  }
}
