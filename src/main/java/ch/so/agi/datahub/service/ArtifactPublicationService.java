package ch.so.agi.datahub.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ArtifactPublicationService {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(ArtifactPublicationService.class);
  private final DeliveryRepository r;
  private final ArtifactStore artifacts;

  public ArtifactPublicationService(DeliveryRepository r, ArtifactStore artifacts) {
    this.r = r;
    this.artifacts = artifacts;
  }

  public void publish(String attempt) {
    var rows =
        r.jdbc()
            .queryForList(
                "SELECT jobid FROM "
                    + r.table("deliveries_delivery")
                    + " WHERE attempt_id=? AND publication_status IN ('PENDING','RETRY')",
                attempt);
    if (rows.isEmpty()) return;
    String delivery = (String) rows.getFirst().get("jobid");
    var row = r.attempt(delivery, attempt);
    if (row == null || !Boolean.TRUE.equals(row.get("isvalid"))) return;
    Path transferTemp = null, logTemp = null;
    try {
      var id = UUID.fromString(delivery);
      var attemptId = UUID.fromString(attempt);
      long version = DeliveryRepository.number(row, "version");
      String theme = DeliveryRepository.string(row, "theme"),
          operat = DeliveryRepository.string(row, "operat");
      Path transfer =
          LocalFilesStorageService.resolve(r.p().getTargetDirectory(), theme, operat + ".xtf");
      Path result =
          LocalFilesStorageService.resolve(r.p().getTargetDirectory(), theme, operat + ".xtf.log");
      Files.createDirectories(transfer.getParent());
      transferTemp = stage(artifacts.input(id), transfer.getParent(), attempt);
      logTemp = stage(artifacts.log(id, attemptId, version), result.getParent(), attempt);
      final Path stagedTransfer = transferTemp, stagedLog = logTemp;
      r.tx()
          .executeWithoutResult(
              status -> {
                long operatId = DeliveryRepository.number(row, "operat_id"),
                    sequence = DeliveryRepository.number(row, "t_id");
                r.jdbc().queryForList("SELECT pg_advisory_xact_lock(?)", operatId);
                var current = r.find(delivery);
                if (!attempt.equals(current.get("attempt_id"))
                    || !Set.of("PENDING", "RETRY").contains(current.get("publication_status")))
                  return;
                var publications =
                    r.jdbc()
                        .queryForList(
                            "SELECT delivery_id FROM "
                                + r.table("dataset_publication")
                                + " WHERE operat_id=?",
                            operatId);
                if (!publications.isEmpty()
                    && DeliveryRepository.number(publications.getFirst(), "delivery_id")
                        > sequence) {
                  r.jdbc()
                      .update(
                          "UPDATE "
                              + r.table("deliveries_delivery")
                              + " SET publication_status='SUPERSEDED',finished_at=?,updated_at=?"
                              + " WHERE jobid=?",
                          r.now(),
                          r.now(),
                          delivery);
                  return;
                }
                try {
                  // Intent is already durable in Delivery. A crash between these moves is repaired
                  // by the next publication pass.
                  Files.move(
                      stagedLog,
                      result,
                      StandardCopyOption.ATOMIC_MOVE,
                      StandardCopyOption.REPLACE_EXISTING);
                  Files.move(
                      stagedTransfer,
                      transfer,
                      StandardCopyOption.ATOMIC_MOVE,
                      StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
                r.jdbc()
                    .update(
                        "INSERT INTO "
                            + r.table("dataset_publication")
                            + "(operat_id,delivery_id) VALUES(?,?) ON CONFLICT(operat_id) DO UPDATE"
                            + " SET delivery_id=EXCLUDED.delivery_id",
                        operatId,
                        sequence);
                r.jdbc()
                    .update(
                        "UPDATE "
                            + r.table("deliveries_delivery")
                            + " SET publication_status='DELIVERED',isdelivered=true,finished_at=?,updated_at=?"
                            + " WHERE jobid=?",
                        r.now(),
                        r.now(),
                        delivery);
              });
    } catch (Exception e) {
      log.error("Publication failed for delivery {}", delivery, e);
      r.tx()
          .executeWithoutResult(
              status -> {
                r.jdbc().queryForList("SELECT pg_advisory_xact_lock(?)", row.get("operat_id"));
                var latest = r.find(delivery);
                if (!attempt.equals(latest.get("attempt_id"))
                    || !Set.of("PENDING", "RETRY").contains(latest.get("publication_status")))
                  return;
                int failures = ((Number) latest.get("publication_tries")).intValue() + 1;
                var next = RetryPolicy.next(failures, r.now());
                r.jdbc()
                    .update(
                        "UPDATE "
                            + r.table("deliveries_delivery")
                            + " SET publication_status=?,publication_tries=?,publication_next_at=?,isdelivered=false,finished_at=?,updated_at=?"
                            + " WHERE jobid=?",
                        next == null ? "FAILED" : "RETRY",
                        failures,
                        next,
                        r.now(),
                        r.now(),
                        delivery);
              });
    } finally {
      for (Path path : new Path[] {transferTemp, logTemp})
        if (path != null)
          try {
            Files.deleteIfExists(path);
          } catch (IOException e) {
            log.warn("Staged publication cleanup deferred for {}", delivery);
          }
    }
  }

  private static Path stage(Path source, Path directory, String attempt) throws IOException {
    Path temp = Files.createTempFile(directory, ".publication-" + attempt + "-", ".tmp");
    try {
      Files.copy(source, temp, StandardCopyOption.REPLACE_EXISTING);
      LocalArtifactStore.force(temp);
      return temp;
    } catch (IOException e) {
      Files.deleteIfExists(temp);
      throw e;
    }
  }
}
