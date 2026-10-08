package ch.so.agi.datahub.ilidata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
@Controller
public class IlidataController {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${app.targetDirectory}")
  private String targetDirectory;

  private final IlidataService ilidataService;

  public IlidataController(IlidataService ilidataService) {
    this.ilidataService = ilidataService;
  }

  @GetMapping(value = "/ilisite.xml")
  public ResponseEntity<?> ilisite() {
    InputStream is = getClass().getResourceAsStream("/ili/ilisite.xml");

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .body(new InputStreamResource(is));
  }

  @GetMapping(value = "/ilimodels.xml")
  public ResponseEntity<?> ilimodels() {
    InputStream is = getClass().getResourceAsStream("/ili/ilimodels.xml");

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .body(new InputStreamResource(is));
  }

  @GetMapping(value = "/ilidata.xml")
  public ResponseEntity<Resource> ilidata() {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .body(new org.springframework.core.io.ByteArrayResource(ilidataService.createXml()));
  }

  @GetMapping(value = "/files/{directory}/{filename}")
  public ResponseEntity<?> getFile(
      @PathVariable("directory") String directory, @PathVariable("filename") String filename) {

    Path xtfFile;
    try {
      xtfFile =
          ch.so.agi.datahub.service.LocalFilesStorageService.resolve(
              targetDirectory, directory, filename);
    } catch (IOException | IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
    if (Files.exists(xtfFile)) {
      Resource resource = new FileSystemResource(xtfFile.toFile());

      return ResponseEntity.ok()
          .header("content-disposition", "attachment; filename=" + xtfFile.getFileName().toString())
          .contentLength(xtfFile.toFile().length())
          .contentType(MediaType.APPLICATION_XML)
          .body(resource);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
