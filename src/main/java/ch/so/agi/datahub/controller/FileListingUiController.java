package ch.so.agi.datahub.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.model.FileEntry;
import ch.so.agi.datahub.model.FileListingViewEntry;
import ch.so.agi.datahub.model.FileListingViewModel;
import ch.so.agi.datahub.service.FileListingService;

@Controller
@RequestMapping("/ui/files")
public class FileListingUiController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FileListingService fileListingService;

    @Value("${app.apiKeyQueryParamName:token}")
    private String apiKeyQueryParamName;

    public FileListingUiController(FileListingService fileListingService) {
        this.fileListingService = fileListingService;
    }

    @GetMapping
    public ModelAndView listFiles(Authentication authentication,
            @RequestParam(name = "path", required = false) String path,
            @RequestParam Map<String, String> params) {
        ensureAdmin(authentication);
        String token = params.get(apiKeyQueryParamName);
        List<FileEntry> entries = fileListingService.listEntries(path);
        List<FileListingViewEntry> viewEntries = entries.stream()
                .map(entry -> toViewEntry(entry))
                .toList();
        FileListingViewModel model = new FileListingViewModel(
                path == null ? "" : path,
                fileListingService.parentPath(path),
                buildTokenQueryParam(token),
                viewEntries
        );
        ModelAndView modelAndView = new ModelAndView("files");
        modelAndView.addObject("model", model);
        return modelAndView;
    }

    private void ensureAdmin(Authentication authentication) {
        if (authentication == null
                || !authentication.getAuthorities().contains(new SimpleGrantedAuthority(AppConstants.ROLE_NAME_ADMIN))) {
            throw new AccessDeniedException("Admin token required.");
        }
    }

    private FileListingViewEntry toViewEntry(FileEntry entry) {
        String encodedRelativePath = URLEncoder.encode(entry.relativePath(), StandardCharsets.UTF_8);
        long sizeSort = entry.directory() ? 0 : entry.size();
        String sizeDisplay = entry.directory() ? "-" : formatSize(entry.size());
        String modifiedDisplay = DATE_FORMATTER.format(entry.modified().atZone(ZoneId.systemDefault()));
        return new FileListingViewEntry(
                entry.name(),
                encodedRelativePath,
                entry.directory(),
                sizeDisplay,
                sizeSort,
                modifiedDisplay,
                entry.modified().toEpochMilli()
        );
    }

    private String buildTokenQueryParam(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }
        return "&" + apiKeyQueryParamName + "=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
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
