package ch.so.agi.datahub.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnWebApplication
public class DeliveryDispatcher {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(DeliveryDispatcher.class);
  private final DeliveryRepository r;
  private final ValidationExecutor executor;

  public DeliveryDispatcher(DeliveryRepository r, ValidationExecutor executor) {
    this.r = r;
    this.executor = executor;
  }

  @Scheduled(fixedDelayString = "${app.dispatchInterval:5000}")
  public void dispatch() {
    r.expireClaims();
    if (!r.p().isDispatchEnabled()) return;
    for (var row :
        r.jdbc()
            .queryForList(
                "SELECT a.id,d.jobid FROM "
                    + r.table("validation_attempt")
                    + " a JOIN "
                    + r.table("deliveries_delivery")
                    + " d ON d.t_id=a.delivery_id WHERE a.dispatch_state='PENDING' AND"
                    + " a.state='ENQUEUED' ORDER BY a.created_at LIMIT 50")) {
      try {
        executor.submit((String) row.get("jobid"), (String) row.get("id"));
        r.jdbc()
            .update(
                "UPDATE "
                    + r.table("validation_attempt")
                    + " SET dispatch_state='SUBMITTED' WHERE id=?",
                row.get("id"));
      } catch (Exception e) {
        log.warn("Dispatch remains pending for attempt {}", row.get("id"));
      }
    }
  }
}
