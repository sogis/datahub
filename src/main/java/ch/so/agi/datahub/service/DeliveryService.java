package ch.so.agi.datahub.service;

import ch.so.agi.datahub.cayenne.DeliveriesDelivery;
import ch.so.agi.datahub.config.AppProperties;
import org.apache.cayenne.CayenneRuntime;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.UUID;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeliveryService {
  private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);
  private final DeliveryAuthorizationService authorization;
  private final CayenneRuntime cayenne;
  private final DeliveryRepository deliveries;
  private final DeliveryFiles files;
  private final IliValidatorService validator;
  private final JobScheduler scheduler;
  private final JavaMailSender mail;
  private final ResourceBundle messages;
  private final AppProperties properties;
  private final Clock clock;

  public DeliveryService(DeliveryAuthorizationService authorization, CayenneRuntime cayenne,
      DeliveryRepository deliveries, DeliveryFiles files, IliValidatorService validator,
      JobScheduler scheduler, JavaMailSender mail, ResourceBundle messages,
      AppProperties properties, Clock clock) {
    this.authorization = authorization;
    this.cayenne = cayenne;
    this.deliveries = deliveries;
    this.files = files;
    this.validator = validator;
    this.scheduler = scheduler;
    this.mail = mail;
    this.messages = messages;
    this.properties = properties;
    this.clock = clock;
  }

  public UUID submit(String theme, String operat, MultipartFile file, Authentication actor)
      throws IOException {
    if (!properties.isAcceptDeliveries())
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Delivery acceptance paused");
    var target = authorization.resolve(theme, operat, actor);
    try {
      DeliveryFiles.publicationSegment(target.theme());
      DeliveryFiles.publicationSegment(target.operat());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Invalid configured delivery target");
    }
    var id = UUID.randomUUID();
    files.saveInput(id, file.getInputStream());

    var context = cayenne.newContext();
    var delivery = context.newObject(DeliveriesDelivery.class);
    delivery.setJobid(id.toString());
    delivery.setDeliverydate(LocalDateTime.now(clock));
    delivery.setOrganisation(target.organisation());
    delivery.setTheme(target.theme());
    delivery.setOperat(target.operat());
    delivery.setEmail(target.email());
    delivery.setValidatorConfig(target.config());
    delivery.setValidatorMetaConfig(target.metaConfig());
    delivery.setExpectedModels(target.expectedModels());
    delivery.setProcessingStatus("PENDING");
    context.commitChanges();

    enqueue(id.toString());
    return id;
  }

  // Restore the JobRunr job if acceptance or a committed publication intent has no job row.
  @Scheduled(fixedDelay = 5000)
  public void recoverUnqueuedDeliveries() {
    deliveries.unqueued().forEach(this::enqueue);
  }

  private void enqueue(String id) {
    try {
      scheduler.enqueue(UUID.fromString(id), () -> process(id));
    } catch (RuntimeException e) {
      log.warn("JobRunr enqueue unavailable for delivery {}", id, e);
    }
  }

  @Job(name = "Delivery %0", retries = 3)
  public void process(String id) throws IOException {
    UUID jobId = UUID.fromString(id);
    // Lock only this delivery during validation. An uncommitted result can be rebuilt from input.
    var validation = deliveries.inTransaction(() -> {
      var delivery = deliveries.lock(id);
      if (delivery == null || !delivery.needsValidation()) return null;
      try {
        var result = validator.validate(files.input(jobId), files.log(jobId), delivery);
        deliveries.validationResult(id, result.valid(), result.transfer());
        return new ValidationStep(result.valid() ? null : delivery, null);
      } catch (IOException e) {
        deliveries.complete(id, "FAILED");
        return new ValidationStep(null, e);
      }
    });
    if (validation != null) {
      if (validation.failure() != null) throw validation.failure();
      if (validation.rejected() != null) {
        notifySupplier(validation.rejected(), "INVALID");
        return;
      }
    }

    // The validation transaction committed the publication intent before any target file changes.
    var publication = deliveries.inTransaction(() -> {
      var delivery = deliveries.lock(id);
      if (delivery == null || !delivery.needsPublication()) return null;
      deliveries.lockTarget(delivery);
      try {
        String outcome = "SUPERSEDED";
        if (!deliveries.hasNewerPublication(delivery)) {
          files.publish(jobId, delivery.theme(), delivery.operat());
          outcome = "DELIVERED";
        }
        deliveries.complete(id, outcome);
        return new PublicationStep(delivery, outcome, null);
      } catch (IOException e) {
        deliveries.complete(id, "PUBLICATION_FAILED");
        return new PublicationStep(null, null, e);
      }
    });
    if (publication != null) {
      if (publication.failure() != null) throw publication.failure();
      notifySupplier(publication.delivery(), publication.outcome());
    }
  }

  private void notifySupplier(DeliveryRepository.Delivery delivery, String outcome) {
    if (!properties.isMailEnabled() || delivery.email() == null) return;
    var id = delivery.id();
    String result = "SENT";
    try {
      var message = new SimpleMailMessage();
      message.setFrom(properties.getMailFrom());
      message.setTo(delivery.email());
      message.setSubject(messages.getString("deliveryEmailSubject")
          .formatted(outcome, delivery.theme(), delivery.operat()));
      message.setText(messages.getString("deliveryEmailBody")
          .formatted(id, properties.publicUrl("/api/logs/" + id), properties.publicUrl("/jobs")));
      mail.send(message);
    } catch (RuntimeException e) {
      result = "FAILED";
      log.warn("Result email could not be sent for delivery {}", id);
    }
    deliveries.notificationResult(id, result);
  }

  private record ValidationStep(DeliveryRepository.Delivery rejected, IOException failure) {}
  private record PublicationStep(DeliveryRepository.Delivery delivery, String outcome, IOException failure) {}
}
