package ch.so.agi.datahub.service;

import java.nio.file.*;
import java.time.*;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "app.cleanerEnabled", havingValue = "true")
public class CleanerService {
  private final DeliveryRepository r;
  private final ArtifactStore artifacts;

  public CleanerService(DeliveryRepository r, ArtifactStore artifacts) {
    this.r = r;
    this.artifacts = artifacts;
  }

  @Scheduled(fixedDelay = 3600000)
  public void cleanUp() throws java.io.IOException {
    var cutoff = r.now().minus(r.p().getArtifactRetention());
    var rows =
        r.jdbc()
            .queryForList(
                "SELECT d.jobid FROM "
                    + r.table("deliveries_delivery")
                    + " d WHERE d.finished_at<? AND d.execution_status IN"
                    + " ('SUCCEEDED','FAILED','DELETED') AND d.publication_status NOT IN"
                    + " ('WAITING','PENDING','RETRY') AND d.notification_status NOT IN"
                    + " ('WAITING','PENDING','RETRY') AND d.artifacts_expired=false AND NOT"
                    + " EXISTS(SELECT 1 FROM "
                    + r.table("dataset_publication")
                    + " p WHERE p.delivery_id=d.t_id)",
                cutoff);
    for (var row : rows) {
      String id = (String) row.get("jobid");
      artifacts.delete(UUID.fromString(id));
      r.jdbc()
          .update(
              "UPDATE "
                  + r.table("deliveries_delivery")
                  + " SET artifacts_expired=true WHERE jobid=?",
              id);
    }
    Path target = Path.of(r.p().getTargetDirectory());
    if (Files.isDirectory(target, LinkOption.NOFOLLOW_LINKS))
      try (var paths = Files.walk(target, 2)) {
        for (var staged : paths.toList()) {
          String name = staged.getFileName().toString();
          if (!Files.isRegularFile(staged, LinkOption.NOFOLLOW_LINKS)
              || !name.startsWith(".publication-")
              || name.length() < 49) continue;
          String attempt = name.substring(13, 49);
          try {
            UUID.fromString(attempt);
          } catch (IllegalArgumentException invalid) {
            continue;
          }
          if (Files.getLastModifiedTime(staged)
                  .toInstant()
                  .isBefore(Instant.now(r.clock()).minus(r.p().getArtifactRetention()))
              && r.jdbc()
                      .queryForObject(
                          "SELECT count(*) FROM "
                              + r.table("deliveries_delivery")
                              + " WHERE attempt_id=? AND publication_status IN ('PENDING','RETRY')",
                          Integer.class,
                          attempt)
                  == 0) Files.deleteIfExists(staged);
        }
      }
    Path root = Path.of(r.p().getWorkDirectory());
    if (!Files.isDirectory(root)) return;
    // Orphan uploads are removed only after a successful database reference lookup.
    try (var paths = Files.list(root)) {
      for (var path : paths.toList())
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
            && path.getFileName().toString().startsWith(r.p().getFolderPrefix())
            && Files.getLastModifiedTime(path)
                .toInstant()
                .isBefore(Instant.now(r.clock()).minus(r.p().getArtifactRetention()))) {
          String id = path.getFileName().toString().substring(r.p().getFolderPrefix().length());
          try {
            UUID.fromString(id);
          } catch (IllegalArgumentException e) {
            continue;
          }
          if (r.find(id) == null) FileSystemUtils.deleteRecursively(path);
        }
    }
    Path workspaces = root.resolve("workspaces");
    if (Files.isDirectory(workspaces, LinkOption.NOFOLLOW_LINKS))
      try (var paths = Files.list(workspaces)) {
        for (var path : paths.toList())
          if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
              && Files.getLastModifiedTime(path)
                  .toInstant()
                  .isBefore(Instant.now(r.clock()).minus(r.p().getArtifactRetention()))) {
            String name = path.getFileName().toString();
            if (name.length() < 37) continue;
            String id = name.substring(0, 36);
            try {
              UUID.fromString(id);
            } catch (IllegalArgumentException e) {
              continue;
            }
            if (r.jdbc()
                    .queryForObject(
                        "SELECT count(*) FROM "
                            + r.table("validation_attempt")
                            + " WHERE id=? AND state='PROCESSING'",
                        Integer.class,
                        id)
                == 0) FileSystemUtils.deleteRecursively(path);
          }
      }
  }
}
