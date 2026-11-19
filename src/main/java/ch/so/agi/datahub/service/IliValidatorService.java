package ch.so.agi.datahub.service;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.datahub.config.AppProperties;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.xml.stream.XMLStreamException;
import org.interlis2.validator.Validator;
import org.springframework.stereotype.Service;

@Service
public class IliValidatorService {
  private final AppProperties properties;

  public IliValidatorService(AppProperties properties) {
    this.properties = properties;
  }

  record Result(boolean valid, Transfer transfer) {}

  record Transfer(Set<String> models, String version) {}

  Result validate(Path input, Path log, DeliveryRepository.Delivery delivery) throws IOException {
    Path temporaryLog = Files.createTempFile(input.getParent(), "validation-", ".log");
    try {
      var result = validate(input, temporaryLog, delivery.expectedModels(), delivery.config(),
          delivery.metaConfig());
      try (var source = Files.newInputStream(temporaryLog)) {
        DeliveryFiles.atomicCopy(source, log);
      }
      return result;
    } catch (RuntimeException e) {
      throw new IOException("Validator failed", e);
    } finally {
      Files.deleteIfExists(temporaryLog);
    }
  }

  private Result validate(Path input, Path log, String expectedModels, String config, String meta)
      throws IOException {
    Transfer transfer;
    try {
      transfer = inspectTransfer(input);
    } catch (XMLStreamException e) {
      Files.writeString(log, "Invalid INTERLIS XML transfer.");
      return new Result(false, new Transfer(Set.of(), null));
    }
    var allowed = Arrays.stream(expectedModels.split(";")).map(String::strip).toList();
    if (transfer.models().isEmpty() || !allowed.containsAll(transfer.models())) {
      Files.writeString(log, "Transfer models are not permitted for the authorized theme.");
      return new Result(false, transfer);
    }

    var settings = new Settings();
    settings.setValue(Validator.SETTING_LOGFILE, log.toString());
    settings.setValue(Validator.SETTING_ILIDIRS, properties.iliRepositories());
    if (config != null && !config.isBlank())
      settings.setValue(Validator.SETTING_CONFIGFILE, "ilidata:" + config);
    if (meta != null && !meta.isBlank())
      settings.setValue(Validator.SETTING_META_CONFIGFILE, "ilidata:" + meta);
    var validator = new Validator();
    boolean valid = validator.validate(new String[]{input.toString()}, settings);
    if (validator.getModel() == null)
      throw new IOException("Validation model could not be resolved or compiled");
    return new Result(valid, transfer);
  }

  static Transfer inspectTransfer(Path file)
      throws java.io.IOException, javax.xml.stream.XMLStreamException {
    var factory = javax.xml.stream.XMLInputFactory.newFactory();
    factory.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
    Set<String> models = new TreeSet<>();
    int depth = 0, dataDepth = -1;
    String version = null;
    try (var input = Files.newInputStream(file)) {
      var reader = factory.createXMLStreamReader(input);
      try {
        while (reader.hasNext()) {
          int event = reader.next();
          if (event == javax.xml.stream.XMLStreamConstants.DTD)
            throw new XMLStreamException("Document type declarations are not permitted");
          if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
            depth++;
            String name = reader.getLocalName();
            if (depth == 1) {
              version = switch (java.util.Objects.toString(reader.getNamespaceURI(), "")) {
                case "http://www.interlis.ch/INTERLIS2.3" -> "2.3";
                case "http://www.interlis.ch/xtf/2.4/INTERLIS" -> "2.4";
                default -> throw new XMLStreamException("Unknown INTERLIS transfer format");
              };
            } else if (depth == 2 && (name.equals("DATASECTION") || name.equals("datasection")))
              dataDepth = depth;
            else if (dataDepth == 2 && depth == 3) {
              String namespace = reader.getNamespaceURI();
              int dot = name.indexOf('.');
              if (dot > 0) models.add(name.substring(0, dot));
              else if (namespace != null && namespace.startsWith("http://www.interlis.ch/xtf/2.4/"))
                models.add(namespace.substring("http://www.interlis.ch/xtf/2.4/".length()));
              else throw new javax.xml.stream.XMLStreamException("Unknown transfer basket");
            }
          } else if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
            if (depth == dataDepth) dataDepth = -1;
            depth--;
          }
        }
      } finally {
        reader.close();
      }
    }
    return new Transfer(models, version);
  }
}
