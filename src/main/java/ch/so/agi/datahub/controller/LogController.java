package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.cayenne.DeliveriesDelivery;
import org.apache.cayenne.CayenneRuntime;
import ch.so.agi.datahub.service.DeliveryFiles;
import org.apache.cayenne.query.ObjectSelect;
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
  private final CayenneRuntime cayenne;
  private final DeliveryFiles files;
  private final AppProperties p;
  private final Clock clock;

  public LogController(
      CayenneRuntime cayenne, DeliveryFiles files, AppProperties p, Clock clock) {
    this.cayenne = cayenne;
    this.files = files;
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
    var delivery = ObjectSelect.query(DeliveriesDelivery.class)
        .where(DeliveriesDelivery.JOBID.eq(id.toString())).selectOne(cayenne.newContext());
    if (delivery == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown delivery");
    if (p.artifactsExpired(delivery.getArtifactsExpired(), delivery.getFinishedAt(), clock))
      throw new ResponseStatusException(HttpStatus.GONE, "Log retention expired");
    Path log = files.log(id);
    if (!Files.isRegularFile(log))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Log is not available");
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
        .contentLength(Files.size(log))
        .body(new FileSystemResource(log));
  }
}
