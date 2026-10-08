package ch.so.agi.datahub.service;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.datahub.config.AppProperties;
import jakarta.annotation.PreDestroy;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.interlis2.validator.Validator;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class ValidationAttemptRunner {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(ValidationAttemptRunner.class);
  private final DeliveryRepository repository;
  private final ArtifactStore artifacts;
  private final DeliveryValidationService validator;
  private final AppProperties p;
  private final Semaphore serial = new Semaphore(1);
  private final ScheduledExecutorService heartbeats =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            var t = new Thread(r, "validation-heartbeat");
            t.setDaemon(true);
            return t;
          });

  public ValidationAttemptRunner(
      DeliveryRepository repository,
      ArtifactStore artifacts,
      DeliveryValidationService validator,
      AppProperties p) {
    this.repository = repository;
    this.artifacts = artifacts;
    this.validator = validator;
    this.p = p;
  }

  @PreDestroy
  void close() {
    heartbeats.shutdownNow();
  }

  public boolean run(String delivery, String attempt) {
    boolean acquired = false;
    ScheduledFuture<?> heartbeat = null;
    Path input = null;
    Long version = null;
    try {
      serial.acquire();
      acquired = true;
      var claim = repository.claim(delivery, attempt);
      if (claim.isEmpty()) {
        var existing = repository.attempt(delivery, attempt);
        return existing != null && "SUCCEEDED".equals(existing.get("attempt_state"));
      }
      version = claim.get();
      final long token = version;
      heartbeat =
          heartbeats.scheduleAtFixedRate(
              () -> {
                try {
                  repository.heartbeat(attempt, token);
                } catch (Exception e) {
                  log.warn("Heartbeat unavailable for attempt {}", attempt);
                }
              },
              10,
              10,
              TimeUnit.SECONDS);
      var row = repository.attempt(delivery, attempt);
      input = artifacts.materialize(UUID.fromString(delivery), UUID.fromString(attempt), version);
      Path result = input.getParent().resolve("result.log");
      Set<String> actual = transferModels(input);
      Set<String> allowed =
          new HashSet<>(
              Arrays.asList(DeliveryRepository.string(row, "expected_models").split(";")));
      boolean valid;
      if (actual.isEmpty() || !allowed.containsAll(actual)) {
        Files.writeString(
            result, "Transfer models are not permitted for the authorized theme: " + actual);
        valid = false;
      } else {
        Settings settings = new Settings();
        settings.setValue(Validator.SETTING_LOGFILE, result.toString());
        settings.setValue(Validator.SETTING_ILIDIRS, p.getPreferredIliRepo());
        var config = DeliveryRepository.string(row, "validator_config");
        var meta = DeliveryRepository.string(row, "validator_meta_config");
        if (config != null && !config.isBlank())
          settings.setValue(Validator.SETTING_CONFIGFILE, "ilidata:" + config);
        if (meta != null && !meta.isBlank())
          settings.setValue(Validator.SETTING_META_CONFIGFILE, "ilidata:" + meta);
        valid = validator.validate(input.toString(), settings);
      }
      if (!Files.exists(result)) Files.writeString(result, "Validation completed: " + valid);
      artifacts.saveResult(UUID.fromString(delivery), UUID.fromString(attempt), version, result);
      return repository.finish(
          delivery,
          attempt,
          version,
          valid,
          String.join(";", new TreeSet<>(actual)),
          "ilivalidator 1.14.3");
    } catch (Exception e) {
      if (e instanceof InterruptedException) Thread.currentThread().interrupt();
      log.error("Technical validation failure for attempt {}", attempt, e);
      if (version != null) {
        try {
          if (input != null) {
            Path result = input.getParent().resolve("result.log");
            Files.writeString(result, "Technical validation failure. Contact service provider.");
            artifacts.saveResult(
                UUID.fromString(delivery), UUID.fromString(attempt), version, result);
          }
          repository.finish(delivery, attempt, version, null, null, "ilivalidator 1.14.3");
        } catch (Exception update) {
          log.error("Cannot record failure for attempt {}", attempt, update);
        }
      }
      return false;
    } finally {
      if (heartbeat != null) heartbeat.cancel(false);
      if (input != null)
        try {
          FileSystemUtils.deleteRecursively(input.getParent());
        } catch (java.io.IOException e) {
          log.warn("Workspace cleanup deferred for attempt {}", attempt);
        }
      if (acquired) serial.release();
    }
  }

  public static Set<String> transferModels(Path file)
      throws java.io.IOException, javax.xml.stream.XMLStreamException {
    var factory = javax.xml.stream.XMLInputFactory.newFactory();
    factory.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
    Set<String> models = new TreeSet<>();
    int depth = 0, dataDepth = -1;
    try (var input = Files.newInputStream(file)) {
      var reader = factory.createXMLStreamReader(input);
      try {
        while (reader.hasNext()) {
          int event = reader.next();
          if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
            depth++;
            String name = reader.getLocalName();
            if (depth == 2 && (name.equals("DATASECTION") || name.equals("datasection")))
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
    return models;
  }
}
