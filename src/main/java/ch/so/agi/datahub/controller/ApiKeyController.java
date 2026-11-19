package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.auth.KeyManagementService;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {
  private final KeyManagementService keys;

  public ApiKeyController(KeyManagementService keys) {
    this.keys = keys;
  }

  @PostMapping
  public ResponseEntity<KeyManagementService.IssuedKey> createApiKey(@RequestPart String organisation) {
    return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
        .body(keys.issue(organisation));
  }

  @DeleteMapping("/{keyId:[0-9a-fA-F-]{36}}")
  public ResponseEntity<Void> deleteApiKey(@PathVariable UUID keyId) {
    if (!keys.revoke(keyId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found.");
    return ResponseEntity.noContent().build();
  }
}
