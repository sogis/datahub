package ch.so.agi.datahub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ch.so.agi.datahub.AppConstants;
import ch.so.agi.datahub.service.FileListingService;

class FileListingUiControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void downloadFileReturnsAttachment() throws Exception {
        FileListingService fileListingService = mock(FileListingService.class);
        FileListingUiController controller = new FileListingUiController(fileListingService);

        Authentication authentication = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority(AppConstants.ROLE_NAME_ADMIN)))
                .when(authentication)
                .getAuthorities();

        Path filePath = Files.createTempFile(tempDir, "sample", ".txt");
        Files.writeString(filePath, "content");
        doReturn(filePath).when(fileListingService).resolveFile("sample.txt");

        ResponseEntity<Resource> response = controller.downloadFile(authentication, "sample.txt");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment")
                .contains(filePath.getFileName().toString());
        assertThat(response.getBody()).isNotNull();
    }
}
