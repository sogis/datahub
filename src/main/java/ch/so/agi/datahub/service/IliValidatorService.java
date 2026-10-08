package ch.so.agi.datahub.service;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.Model;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import org.interlis2.validator.Validator;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class IliValidatorService implements DeliveryValidationService {
  @Override
  public ValidationOutcome validate(String filePath, Settings settings) throws Exception {
    var validator = new Validator();
    boolean valid = validator.validate(new String[] {filePath}, settings);
    var models = new ArrayList<Map<String, Object>>();
    var td = validator.getModel();
    if (td == null)
      throw new java.io.IOException("Validation model could not be resolved or compiled");
    for (var iterator = td.iterator(); iterator.hasNext(); ) {
      var element = iterator.next();
      if (element instanceof Model model && !"INTERLIS".equals(model.getName())) {
        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("name", model.getName());
        metadata.put("version", model.getModelVersion());
        metadata.put("iliVersion", model.getIliVersion());
        metadata.put("source", snapshot(model.getFileName()));
        models.add(metadata);
      }
    }
    var configurations = new LinkedHashMap<String, Object>();
    configurations.put("config", settings.getValue(Validator.SETTING_CONFIGFILE));
    configurations.put("metaConfig", settings.getValue(Validator.SETTING_META_CONFIGFILE));
    configurations.put("modelRepositories", settings.getValue(Validator.SETTING_ILIDIRS));
    // Validator logs the concrete local files after resolving ilidata references and includes.
    var resolved = new LinkedHashMap<String, Object>();
    var pattern = java.util.regex.Pattern.compile("(?:configFile|metaConfigFile) <([^>]+)>");
    try (var lines = Files.lines(Path.of(settings.getValue(Validator.SETTING_LOGFILE)))) {
      lines.forEach(
          line -> {
            var match = pattern.matcher(line);
            if (match.find()) resolved.put(match.group(1), snapshot(match.group(1)));
          });
    }
    configurations.put("resolvedFiles", resolved);
    var mapper = JsonMapper.builder().build();
    return new ValidationOutcome(
        valid, mapper.writeValueAsString(models), mapper.writeValueAsString(configurations));
  }

  private static Map<String, Object> snapshot(String file) {
    var result = new LinkedHashMap<String, Object>();
    result.put("path", file);
    if (file != null)
      try {
        byte[] bytes = Files.readAllBytes(Path.of(file));
        result.put(
            "sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        result.put("content", new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
      } catch (Exception e) {
        result.put("available", false);
      }
    return result;
  }
}
