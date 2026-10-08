package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.service.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
@RestController
public class LogController {
  private final DeliveryRepository repository;
  private final ArtifactStore artifacts;
  private final AppProperties p;
  private final Clock clock;

  public LogController(
      DeliveryRepository repository, ArtifactStore artifacts, AppProperties p, Clock clock) {
    this.repository = repository;
    this.artifacts = artifacts;
    this.p = p;
    this.clock = clock;
  }

  @GetMapping("/api/logs/{jobId}")
  public ResponseEntity<?> getLogById(@PathVariable String jobId) throws IOException {
    UUID id;
    try {
      id = UUID.fromString(jobId);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown delivery");
    }
    var row = repository.find(jobId);
    if (row == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown delivery");
    var finished = DeliveryRepository.time(row, "finished_at");
    if (Boolean.TRUE.equals(row.get("artifacts_expired"))
        || (finished != null
            && finished.isBefore(LocalDateTime.now(clock).minus(p.getArtifactRetention()))))
      throw new ResponseStatusException(HttpStatus.GONE, "Log retention expired");
    Path log;
    if (row.get("attempt_id") == null)
      log =
          LocalFilesStorageService.resolve(
              p.getWorkDirectory(), p.getFolderPrefix() + id, id + ".log");
    else {
      var attempt = repository.attempt(jobId, (String) row.get("attempt_id"));
      log =
          artifacts.log(
              id,
              UUID.fromString((String) row.get("attempt_id")),
              DeliveryRepository.number(attempt, "version"));
    }
    if (!Files.isRegularFile(log))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Log is not available");
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
        .contentLength(Files.size(log))
        .body(new FileSystemResource(log));
  }
}
