package ch.so.agi.datahub.service;

import static org.assertj.core.api.Assertions.*;

import ch.so.agi.datahub.config.AppProperties;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArtifactStoreTest {
  @Test
  void immutableInputMaterializationAndResult(@TempDir Path root) throws Exception {
    var p = new AppProperties();
    p.setWorkDirectory(root.toString());
    var store = new LocalArtifactStore(p);
    var delivery = UUID.randomUUID();
    var attempt = UUID.randomUUID();
    store.saveInput(delivery, new ByteArrayInputStream("original".getBytes()));
    assertThatThrownBy(
            () -> store.saveInput(delivery, new ByteArrayInputStream("replacement".getBytes())))
        .isInstanceOf(IOException.class);
    var input = store.materialize(delivery, attempt, 1);
    Files.writeString(input, "changed workspace");
    assertThat(Files.readString(store.input(delivery))).isEqualTo("original");
    var log = input.getParent().resolve("result.log");
    Files.writeString(log, "result");
    store.saveResult(delivery, attempt, 1, log);
    assertThatThrownBy(() -> store.saveResult(delivery, attempt, 1, log))
        .isInstanceOf(IOException.class);
    assertThat(Files.readString(store.log(delivery, attempt, 1))).isEqualTo("result");
  }

  @Test
  void rejectsTraversalAndSymlinkOutsideRoot(@TempDir Path root) throws Exception {
    assertThatThrownBy(
            () -> LocalFilesStorageService.resolve(root.toString(), "../outside", "file.xtf"))
        .isInstanceOf(IllegalArgumentException.class);
    Files.createSymbolicLink(root.resolve("escape"), root.getParent());
    assertThatThrownBy(
            () -> LocalFilesStorageService.resolve(root.toString(), "escape", "file.xtf"))
        .isInstanceOf(IOException.class);
  }

  @Test
  void authoritativeModelsCompile() {
    var config = new ch.interlis.ili2c.config.Configuration();
    config.addFileEntry(
        new ch.interlis.ili2c.config.FileEntry(
            "dev/SO_AGI_Datahub_20240403.ili",
            ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));
    assertThat(ch.interlis.ili2c.Main.runCompiler(config)).isNotNull();
  }

  @Test
  void boundedRetrySchedule() {
    var now = LocalDateTime.of(2026, 10, 8, 0, 0);
    assertThat(RetryPolicy.next(1, now)).isEqualTo(now.plusMinutes(1));
    assertThat(RetryPolicy.next(2, now)).isEqualTo(now.plusMinutes(5));
    assertThat(RetryPolicy.next(3, now)).isEqualTo(now.plusMinutes(15));
    assertThat(RetryPolicy.next(4, now)).isNull();
  }

  @Test
  void readsRealTransferModels() throws Exception {
    assertThat(ValidationAttemptRunner.transferModels(Path.of("dev/valid.xtf")))
        .containsExactly("DatahubDemo");
    assertThat(
            ValidationAttemptRunner.transferModels(
                Path.of("src/test/data/DMAV_Dienstbarkeitsgrenzen_V1_0.449.xtf")))
        .containsExactly("DMAV_Dienstbarkeitsgrenzen_V1_0");
    assertThat(
            ValidationAttemptRunner.transferVersion(
                Path.of("src/test/data/DMAV_Dienstbarkeitsgrenzen_V1_0.449.xtf")))
        .isEqualTo("2.4");
  }
}
