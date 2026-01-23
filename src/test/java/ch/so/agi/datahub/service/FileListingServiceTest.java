package ch.so.agi.datahub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.so.agi.datahub.model.FileEntry;

class FileListingServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void listsEntriesSortedWithDirectoriesFirst() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("docs"));
        Path file = Files.writeString(tempDir.resolve("alpha.txt"), "content");
        Files.writeString(subDir.resolve("beta.txt"), "content");

        FileListingService service = new FileListingService(tempDir.toString());
        List<FileEntry> entries = service.listEntries("");

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).directory()).isTrue();
        assertThat(entries.get(0).name()).isEqualTo("docs");
        assertThat(entries.get(1).directory()).isFalse();
        assertThat(entries.get(1).name()).isEqualTo("alpha.txt");
        assertThat(entries.get(1).size()).isEqualTo(Files.size(file));
        assertThat(entries.get(1).modified()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void rejectsPathTraversalOutsideRoot() {
        FileListingService service = new FileListingService(tempDir.toString());
        assertThatThrownBy(() -> service.listEntries("../"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid path");
    }
}
