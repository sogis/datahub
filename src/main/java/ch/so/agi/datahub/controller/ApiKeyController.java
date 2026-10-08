package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.auth.KeyManagementService;
import ch.so.agi.datahub.model.GenericResponse;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
@RestController
public class ApiKeyController {
  private final KeyManagementService keys;
  private final Clock clock;

  public ApiKeyController(KeyManagementService keys, Clock clock) {
    this.keys = keys;
    this.clock = clock;
  }

  @PostMapping("/api/keys")
  public ResponseEntity<?> createApiKey(
      Authentication actor,
      @RequestPart(name = "organisation", required = false) String organisation) {
    keys.issue(actor, organisation);
    return ResponseEntity.ok(
        new GenericResponse(null, "Sent email with new api key.", Instant.now(clock)));
  }

  @DeleteMapping("/api/keys/{apiKey}")
  public ResponseEntity<?> deleteApiKey(Authentication actor, @PathVariable String apiKey) {
    if (!keys.revoke(actor, apiKey))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "API key not deleted.");
    return ResponseEntity.ok(new GenericResponse(null, "API key deleted.", Instant.now(clock)));
  }
}
