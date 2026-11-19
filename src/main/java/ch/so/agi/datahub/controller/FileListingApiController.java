package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.model.FileEntry;
import ch.so.agi.datahub.service.FileListingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
@RestController
@RequestMapping("/api/files")
public class FileListingApiController {
  private final FileListingService fileListingService;

  public FileListingApiController(FileListingService fileListingService) {
    this.fileListingService = fileListingService;
  }

  @GetMapping
  public ResponseEntity<List<FileEntry>> listFiles(
      @RequestParam(name = "path", required = false) String path) {
    return ResponseEntity.ok(fileListingService.listEntries(path));
  }

}
