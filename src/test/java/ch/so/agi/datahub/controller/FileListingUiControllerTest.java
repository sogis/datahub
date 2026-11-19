package ch.so.agi.datahub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import ch.so.agi.datahub.service.FileListingService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

class FileListingUiControllerTest {

  @TempDir Path tempDir;

  @Test
  void downloadFileReturnsAttachment() throws Exception {
    FileListingService fileListingService = mock(FileListingService.class);
    FileListingUiController controller =
        new FileListingUiController(
            fileListingService, new ch.so.agi.datahub.config.AppProperties());

    Path filePath = Files.createTempFile(tempDir, "sample", ".txt");
    Files.writeString(filePath, "content");
    doReturn(filePath).when(fileListingService).resolveFile("sample.txt");

    ResponseEntity<Resource> response = controller.downloadFile("sample.txt");

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
        .contains("attachment")
        .contains(filePath.getFileName().toString());
    assertThat(response.getBody()).isNotNull();
  }
}
