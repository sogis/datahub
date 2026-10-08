package ch.so.agi.datahub.service;

import ch.so.agi.datahub.auth.ApiKeyUser;
import ch.so.agi.datahub.cayenne.DeliveriesDelivery;
import ch.so.agi.datahub.cayenne.ValidationAttempt;
import ch.so.agi.datahub.config.*;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.apache.cayenne.Cayenne;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DeliverySubmissionService {
  public record Command(String theme, String operat, MultipartFile file) {}

  private final DeliveryAuthorizationService authorization;
  private final CayenneContexts contexts;
  private final ArtifactStore artifacts;
  private final AppProperties p;
  private final Clock clock;

  public DeliverySubmissionService(
      DeliveryAuthorizationService authorization,
      CayenneContexts contexts,
      ArtifactStore artifacts,
      AppProperties p,
      Clock clock) {
    this.authorization = authorization;
    this.contexts = contexts;
    this.artifacts = artifacts;
    this.p = p;
    this.clock = clock;
  }

  public UUID submit(String theme, String operat, MultipartFile file, Authentication actor)
      throws IOException {
    return submit(new Command(theme, operat, file), actor);
  }

  public UUID submit(Command command, Authentication actor) throws IOException {
    var theme = command.theme();
    var operat = command.operat();
    var file = command.file();
    if (!p.isAcceptDeliveries())
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Delivery acceptance paused");
    var dataset = authorization.resolve(theme, operat, actor);
    LocalFilesStorageService.segment(dataset.theme());
    LocalFilesStorageService.segment(dataset.operat());
    UUID deliveryId = UUID.randomUUID(), attemptId = UUID.randomUUID();
    artifacts.saveInput(deliveryId, file.getInputStream());
    // Both commits join the same Cayenne transaction. The first obtains the database-generated
    // delivery identity.
    contexts.transaction(
        ctx -> {
          var d = ctx.newObject(DeliveriesDelivery.class);
          d.setJobid(deliveryId.toString());
          d.setDeliverydate(LocalDateTime.now(clock));
          d.setUpdatedAt(LocalDateTime.now(clock));
          d.setOperatId(dataset.id());
          d.setOrganisation(dataset.organisation());
          d.setTheme(dataset.theme());
          d.setOperat(dataset.operat());
          d.setKeyId(((ApiKeyUser) actor.getPrincipal()).getKeyId().toString());
          d.setAttemptId(attemptId.toString());
          d.setEmail(dataset.email());
          d.setValidatorConfig(dataset.config());
          d.setValidatorMetaConfig(dataset.metaConfig());
          d.setExpectedModels(dataset.expectedModels());
          d.setExecutionStatus("ENQUEUED");
          d.setPublicationStatus("WAITING");
          d.setNotificationStatus(p.isMailEnabled() ? "WAITING" : "DISABLED");
          d.setPublicationTries(0);
          d.setArtifactsExpired(false);
          ctx.commitChanges();
          var attempt = ctx.newObject(ValidationAttempt.class);
          attempt.setId(attemptId.toString());
          attempt.setDeliveryId(Cayenne.longPKForObject(d));
          attempt.setCreatedAt(LocalDateTime.now(clock));
          attempt.setState("ENQUEUED");
          attempt.setDispatchState("PENDING");
          attempt.setVersion(0L);
          ctx.commitChanges();
          return null;
        });
    return deliveryId;
  }
}
