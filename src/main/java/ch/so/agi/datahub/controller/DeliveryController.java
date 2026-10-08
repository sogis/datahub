package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.service.DeliverySubmissionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
@RestController
public class DeliveryController {
  private final DeliverySubmissionService service;
  private final AppProperties p;

  public DeliveryController(DeliverySubmissionService service, AppProperties p) {
    this.service = service;
    this.p = p;
  }

  @PostMapping(value = "/api/deliveries", consumes = "multipart/form-data")
  public ResponseEntity<?> uploadFile(
      Authentication actor,
      @RequestPart String theme,
      @RequestPart String operat,
      @RequestPart MultipartFile file,
      HttpServletRequest request)
      throws IOException, ServletException {
    validate(request, Map.of("theme", theme, "operat", operat));
    if (file.isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty transfer file");
    var id = service.submit(theme, operat, file, actor);
    return ResponseEntity.accepted()
        .header("Operation-Location", p.publicUrl("/api/jobs/" + id))
        .build();
  }

  static void validate(HttpServletRequest request, Map<String, String> fields)
      throws IOException, ServletException {
    for (String name : List.of("theme", "operat", "file")) {
      if (request.getParts().stream().filter(part -> part.getName().equals(name)).count() != 1)
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Duplicate or missing multipart field: " + name);
    }
    Map<String, List<String>> query = new HashMap<>();
    if (request.getQueryString() != null)
      for (String pair : request.getQueryString().split("&")) {
        String[] kv = pair.split("=", 2);
        String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
        if (fields.containsKey(key))
          query
              .computeIfAbsent(key, k -> new ArrayList<>())
              .add(URLDecoder.decode(kv.length == 2 ? kv[1] : "", StandardCharsets.UTF_8));
      }
    for (var entry : query.entrySet())
      if (entry.getValue().size() != 1
          || !entry.getValue().getFirst().equals(fields.get(entry.getKey())))
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Conflicting delivery parameters");
  }
}
