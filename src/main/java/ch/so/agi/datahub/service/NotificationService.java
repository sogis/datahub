package ch.so.agi.datahub.service;

import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(NotificationService.class);
  private final DeliveryRepository r;
  private final EmailService mail;

  public NotificationService(DeliveryRepository r, EmailService mail) {
    this.r = r;
    this.mail = mail;
  }

  public void prepare() {
    r.tx()
        .executeWithoutResult(
            status -> {
              var rows =
                  r.jdbc()
                      .queryForList(
                          "SELECT * FROM "
                              + r.table("deliveries_delivery")
                              + " WHERE notification_status='WAITING' AND execution_status IN"
                              + " ('SUCCEEDED','FAILED') AND publication_status NOT IN"
                              + " ('WAITING','PENDING','RETRY') FOR UPDATE SKIP LOCKED LIMIT 50");
              for (var row : rows) {
                String id = (String) row.get("jobid");
                if (!r.p().isMailEnabled()) {
                  r.jdbc()
                      .update(
                          "UPDATE "
                              + r.table("deliveries_delivery")
                              + " SET notification_status='DISABLED' WHERE jobid=?",
                          id);
                  continue;
                }
                String result = String.valueOf(row.get("validation_status"));
                String subject =
                    "Datahub " + result + " – " + row.get("theme") + " / " + row.get("operat");
                String body =
                    "Lieferung: "
                        + id
                        + "\nAusführung: "
                        + row.get("execution_status")
                        + "\nValidierung: "
                        + result
                        + "\nÜbernahme: "
                        + row.get("publication_status")
                        + "\nProtokoll: "
                        + r.p().publicUrl("/api/logs/" + id)
                        + "\nÜbersicht: "
                        + r.p().publicUrl("/jobs");
                r.jdbc()
                    .update(
                        "INSERT INTO "
                            + r.table("notification")
                            + "(id,delivery_id,recipient,subject,body,state,tries,next_at)"
                            + " VALUES(?,?,?,?,?,'PENDING',0,?) ON CONFLICT(delivery_id) DO"
                            + " NOTHING",
                        id,
                        row.get("t_id"),
                        row.get("email"),
                        subject,
                        body,
                        r.now());
                r.jdbc()
                    .update(
                        "UPDATE "
                            + r.table("deliveries_delivery")
                            + " SET notification_status='PENDING' WHERE jobid=?",
                        id);
              }
            });
  }

  public void sendDue() {
    if (!r.p().isMailEnabled()) return;
    for (int n = 0; n < 50; n++) {
      var task =
          r.tx()
              .execute(
                  status -> {
                    var rows =
                        r.jdbc()
                            .queryForList(
                                "SELECT * FROM "
                                    + r.table("notification")
                                    + " WHERE state IN ('PENDING','RETRY') AND next_at<=? AND"
                                    + " (lease_until IS NULL OR lease_until<?) ORDER BY next_at FOR"
                                    + " UPDATE SKIP LOCKED LIMIT 1",
                                r.now(),
                                r.now());
                    if (rows.isEmpty()) return null;
                    var row = rows.getFirst();
                    r.jdbc()
                        .update(
                            "UPDATE " + r.table("notification") + " SET lease_until=? WHERE id=?",
                            r.now().plusMinutes(2),
                            row.get("id"));
                    return row;
                  });
      if (task == null) return;
      String state = "SENT";
      int failures = ((Number) task.get("tries")).intValue();
      java.time.LocalDateTime next = r.now();
      try {
        mail.send(
            (String) task.get("recipient"),
            (String) task.get("subject"),
            (String) task.get("body"));
      } catch (Exception e) {
        failures++;
        next = RetryPolicy.next(failures, r.now());
        state = next == null ? "FAILED" : "RETRY";
        log.warn("Email remains {} for notification {}", state, task.get("id"));
      }
      final String outcome = state;
      final int tries = failures;
      final java.time.LocalDateTime due = next;
      r.tx()
          .executeWithoutResult(
              status -> {
                r.jdbc()
                    .update(
                        "UPDATE "
                            + r.table("notification")
                            + " SET state=?,tries=?,next_at=?,lease_until=NULL WHERE id=?",
                        outcome,
                        tries,
                        due == null ? r.now() : due,
                        task.get("id"));
                if (task.get("delivery_id") != null)
                  r.jdbc()
                      .update(
                          "UPDATE "
                              + r.table("deliveries_delivery")
                              + " SET notification_status=?,updated_at=? WHERE t_id=?",
                          outcome,
                          r.now(),
                          task.get("delivery_id"));
              });
    }
  }
}
