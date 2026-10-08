package ch.so.agi.datahub.controller;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.model.FileEntry;
import ch.so.agi.datahub.model.FileListingViewEntry;
import ch.so.agi.datahub.model.FileListingViewModel;
import ch.so.agi.datahub.service.FileListingService;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
@Controller
@RequestMapping("/ui/files")
public class FileListingUiController {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final FileListingService fileListingService;

  private final AppProperties properties;

  public FileListingUiController(FileListingService fileListingService, AppProperties properties) {
    this.fileListingService = fileListingService;
    this.properties = properties;
  }

  @GetMapping(produces = "text/html")
  public ModelAndView listFiles(
      Authentication authentication,
      @RequestParam(name = "path", required = false) String path,
      @RequestParam Map<String, String> params,
      jakarta.servlet.http.HttpServletRequest request) {
    ensureAdmin(authentication);
    String token = params.get(properties.getApiKeyQueryParamName());
    List<FileEntry> entries = fileListingService.listEntries(path);
    List<FileListingViewEntry> viewEntries =
        entries.stream().map(entry -> toViewEntry(entry)).toList();
    FileListingViewModel model =
        new FileListingViewModel(
            path == null ? "" : path,
            fileListingService.parentPath(path) == null
                ? null
                : URLEncoder.encode(fileListingService.parentPath(path), StandardCharsets.UTF_8),
            buildTokenQueryParam(token),
            viewEntries);
    ModelAndView modelAndView = new ModelAndView("files");
    modelAndView.addObject("model", model);
    modelAndView.addObject("context", request.getContextPath());
    return modelAndView;
  }

  @GetMapping(value = "/download")
  public ResponseEntity<Resource> downloadFile(
      Authentication authentication, @RequestParam(name = "path") String path) {
    ensureAdmin(authentication);
    Path filePath = fileListingService.resolveFile(path);
    Resource resource = new FileSystemResource(filePath);
    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
    try {
      String contentType = Files.probeContentType(filePath);
      if (contentType != null && !contentType.isBlank()) {
        mediaType = MediaType.parseMediaType(contentType);
      }
    } catch (IOException e) {
      mediaType = MediaType.APPLICATION_OCTET_STREAM;
    }
    ContentDisposition contentDisposition =
        ContentDisposition.attachment().filename(filePath.getFileName().toString()).build();
    return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .body(resource);
  }

  private void ensureAdmin(Authentication authentication) {
    if (authentication == null
        || !authentication
            .getAuthorities()
            .contains(new SimpleGrantedAuthority(AppConstants.ROLE_NAME_ADMIN))) {
      throw new AccessDeniedException("Admin token required.");
    }
  }

  private FileListingViewEntry toViewEntry(FileEntry entry) {
    String encodedRelativePath = URLEncoder.encode(entry.relativePath(), StandardCharsets.UTF_8);
    long sizeSort = entry.directory() ? 0 : entry.size();
    String sizeDisplay = entry.directory() ? "-" : formatSize(entry.size());
    String modifiedDisplay =
        DATE_FORMATTER.format(entry.modified().atZone(ZoneId.of(properties.getTimeZone())));
    return new FileListingViewEntry(
        entry.name(),
        encodedRelativePath,
        entry.directory(),
        sizeDisplay,
        sizeSort,
        modifiedDisplay,
        entry.modified().toEpochMilli());
  }

  private String buildTokenQueryParam(String token) {
    if (token == null || token.isBlank()) {
      return "";
    }
    return "&"
        + properties.getApiKeyQueryParamName()
        + "="
        + URLEncoder.encode(token, StandardCharsets.UTF_8);
  }

  private String formatSize(long size) {
    if (size < 1024) {
      return size + " B";
    }
    double kb = size / 1024.0;
    if (kb < 1024) {
      return String.format("%.1f KB", kb);
    }
    double mb = kb / 1024.0;
    if (mb < 1024) {
      return String.format("%.1f MB", mb);
    }
    double gb = mb / 1024.0;
    return String.format("%.1f GB", gb);
  }
}
