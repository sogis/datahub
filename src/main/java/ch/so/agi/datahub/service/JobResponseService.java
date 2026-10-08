package ch.so.agi.datahub.service;

import ch.so.agi.datahub.model.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class JobResponseService {
  private final DeliveryRepository r;

  public JobResponseService(DeliveryRepository r) {
    this.r = r;
  }

  public java.util.List<JobResponse> getJobResponseList() {
    return search(JobSearch.all(), r.p().getJobResponseListLimit()).rows();
  }

  public JobPage search(JobSearch search, int size) {
    List<Object> params = new ArrayList<>();
    StringBuilder where = new StringBuilder("d.deliverydate>=?");
    params.add(r.now().minusDays(14));
    add(where, params, "d.organisation", search.organisation(), false);
    add(where, params, "d.theme", search.theme(), true);
    add(where, params, "d.operat", search.operat(), true);
    add(where, params, "d.execution_status", search.status(), false);
    add(where, params, "d.validation_status", search.validationStatus(), false);
    int count =
        r.jdbc()
            .queryForObject(
                "SELECT count(*) FROM " + r.table("deliveries_delivery") + " d WHERE " + where,
                Integer.class,
                params.toArray());
    String order =
        switch (search.sort()) {
          case "date-asc" -> "d.deliverydate ASC,d.t_id ASC";
          case "theme" -> "d.theme ASC,d.t_id DESC";
          case "operat" -> "d.operat ASC,d.t_id DESC";
          default -> "d.deliverydate DESC,d.t_id DESC";
        };
    int pageSize = Math.max(1, Math.min(size, r.p().getJobResponseListLimit()));
    int page = search.page();
    params.add(pageSize);
    params.add((long) page * pageSize);
    var rows =
        r.jdbc()
            .queryForList(
                queueCte()
                    + " SELECT d.*,q.queueposition FROM "
                    + r.table("deliveries_delivery")
                    + " d LEFT JOIN queued q ON q.jobid=d.jobid WHERE "
                    + where
                    + " ORDER BY "
                    + order
                    + " LIMIT ? OFFSET ?",
                params.toArray());
    return new JobPage(rows.stream().map(this::response).toList(), count, page, pageSize);
  }

  public JobResponse getJobResponseById(String id) {
    var rows =
        r.jdbc()
            .queryForList(
                queueCte()
                    + " SELECT d.*,q.queueposition FROM "
                    + r.table("deliveries_delivery")
                    + " d LEFT JOIN queued q ON q.jobid=d.jobid WHERE d.jobid=?",
                id);
    return rows.isEmpty() ? null : response(rows.getFirst());
  }

  private String queueCte() {
    return "WITH queued AS(SELECT jobid,ROW_NUMBER() OVER(ORDER BY deliverydate,t_id) AS"
               + " queueposition FROM "
        + r.table("deliveries_delivery")
        + " WHERE execution_status='ENQUEUED')";
  }

  private static void add(
      StringBuilder where, List<Object> args, String column, String value, boolean prefix) {
    if (value == null || value.isBlank()) return;
    where.append(" AND ").append(column).append(prefix ? " ILIKE ? ESCAPE '\\'" : "=?");
    args.add(
        prefix ? value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%" : value);
  }

  private JobResponse response(Map<String, Object> row) {
    String id = (String) row.get("jobid");
    LocalDateTime created = DeliveryRepository.time(row, "deliverydate"),
        updated = DeliveryRepository.time(row, "updated_at");
    String validation = (String) row.get("validation_status");
    boolean expired = Boolean.TRUE.equals(row.get("artifacts_expired"));
    var finished = DeliveryRepository.time(row, "finished_at");
    expired |= finished != null && finished.isBefore(r.now().minus(r.p().getArtifactRetention()));
    var response =
        new JobResponse(
            id,
            created,
            updated == null ? created : updated,
            (String) row.get("execution_status"),
            row.get("queueposition") == null
                ? null
                : ((Number) row.get("queueposition")).longValue(),
            (String) row.get("operat"),
            (String) row.get("theme"),
            (String) row.get("organisation"),
            expired ? "Log retention expired" : null,
            validation,
            finished == null ? null : r.p().publicUrl("/api/logs/" + id),
            null,
            null);
    response.setDeliveryStatus((String) row.get("publication_status"));
    response.setNotificationStatus((String) row.get("notification_status"));
    return response;
  }
}
