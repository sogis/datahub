package ch.so.agi.datahub.service;

import static org.assertj.core.api.Assertions.*;

import ch.so.agi.datahub.config.AppProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryFilesTest {
  @Test
  void anAcceptedInputCannotBeReplaced(@TempDir Path root) throws Exception {
    var properties = new AppProperties();
    properties.setWorkDirectory(root.toString());
    var files = new DeliveryFiles(properties);
    var id = UUID.randomUUID();
    files.saveInput(id, new ByteArrayInputStream(new byte[]{1, 2, 3}));
    assertThatThrownBy(() -> files.saveInput(id, new ByteArrayInputStream(new byte[]{4})))
        .isInstanceOf(IOException.class);
    assertThat(Files.readAllBytes(files.input(id))).containsExactly(1, 2, 3);
  }

  @Test
  void rejectsTraversalAndSymlinkOutsideRoot(@TempDir Path root) throws Exception {
    assertThatThrownBy(() -> DeliveryFiles.resolve(root.toString(), "../outside", "file.xtf"))
        .isInstanceOf(IllegalArgumentException.class);
    Files.createSymbolicLink(root.resolve("escape"), root.getParent());
    assertThatThrownBy(() -> DeliveryFiles.resolve(root.toString(), "escape", "file.xtf"))
        .isInstanceOf(IOException.class);
  }

  @Test
  void recognizesBothTransferFormats() throws Exception {
    assertThat(IliValidatorService.inspectTransfer(Path.of("dev/valid.xtf")).models())
        .containsExactly("DatahubDemo");
    var transfer = IliValidatorService.inspectTransfer(
        Path.of("src/test/data/DMAV_Dienstbarkeitsgrenzen_V1_0.449.xtf"));
    assertThat(transfer.models()).containsExactly("DMAV_Dienstbarkeitsgrenzen_V1_0");
    assertThat(transfer.version()).isEqualTo("2.4");
  }
}
