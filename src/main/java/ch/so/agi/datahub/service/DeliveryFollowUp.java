package ch.so.agi.datahub.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnWebApplication
public class DeliveryFollowUp {
  private final DeliveryRepository r;
  private final ArtifactPublicationService publications;
  private final NotificationService notifications;

  public DeliveryFollowUp(
      DeliveryRepository r,
      ArtifactPublicationService publications,
      NotificationService notifications) {
    this.r = r;
    this.publications = publications;
    this.notifications = notifications;
  }

  @Scheduled(fixedDelayString = "${app.followUpInterval:5000}")
  public void process() {
    for (var row :
        r.jdbc()
            .queryForList(
                "SELECT attempt_id FROM "
                    + r.table("deliveries_delivery")
                    + " WHERE publication_status IN ('PENDING','RETRY') AND (publication_next_at IS"
                    + " NULL OR publication_next_at<=?) ORDER BY t_id LIMIT 20",
                r.now())) publications.publish((String) row.get("attempt_id"));
    notifications.prepare();
    notifications.sendDue();
  }
}
