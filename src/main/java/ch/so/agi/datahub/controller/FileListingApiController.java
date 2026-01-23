package ch.so.agi.datahub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.model.FileEntry;
import ch.so.agi.datahub.service.FileListingService;

@RestController
@RequestMapping("/api/files")
public class FileListingApiController {
    private final FileListingService fileListingService;

    public FileListingApiController(FileListingService fileListingService) {
        this.fileListingService = fileListingService;
    }

    @GetMapping
    public ResponseEntity<List<FileEntry>> listFiles(Authentication authentication,
            @RequestParam(name = "path", required = false) String path) {
        ensureAdmin(authentication);
        return ResponseEntity.ok(fileListingService.listEntries(path));
    }

    private void ensureAdmin(Authentication authentication) {
        if (authentication == null
                || !authentication.getAuthorities().contains(new SimpleGrantedAuthority(AppConstants.ROLE_NAME_ADMIN))) {
            throw new AccessDeniedException("Admin token required.");
        }
    }
}
