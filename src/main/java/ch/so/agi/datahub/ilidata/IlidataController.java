package ch.so.agi.datahub.ilidata;

import ch.so.agi.datahub.config.AppProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
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

  private final AppProperties properties;

  private final IlidataService ilidataService;

  public IlidataController(IlidataService ilidataService, AppProperties properties) {
    this.ilidataService = ilidataService;
    this.properties = properties;
  }

  @GetMapping(value = "/ilisite.xml")
  public ResponseEntity<?> ilisite() {
    var resource = new org.springframework.core.io.ClassPathResource("ili/ilisite.xml");

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(resource);
  }

  @GetMapping(value = "/ilimodels.xml")
  public ResponseEntity<?> ilimodels() {
    var resource = new org.springframework.core.io.ClassPathResource("ili/ilimodels.xml");

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(resource);
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
              properties.getTargetDirectory(), directory, filename);
    } catch (IOException | IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
    if (Files.isRegularFile(xtfFile)) {
      Resource resource = new FileSystemResource(xtfFile.toFile());

      return ResponseEntity.ok()
          .header(
              "content-disposition",
              org.springframework.http.ContentDisposition.attachment()
                  .filename(
                      xtfFile.getFileName().toString(), java.nio.charset.StandardCharsets.UTF_8)
                  .build()
                  .toString())
          .contentLength(xtfFile.toFile().length())
          .contentType(MediaType.APPLICATION_XML)
          .body(resource);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
