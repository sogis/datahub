package ch.so.agi.datahub.service;

import ch.so.agi.datahub.model.*;
import ch.so.agi.datahub.config.AppProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class JobResponseService {
  private final JdbcTemplate jdbc;
  private final AppProperties properties;
  private final Clock clock;

  public JobResponseService(JdbcTemplate jdbc, AppProperties properties, Clock clock) {
    this.jdbc = jdbc;
    this.properties = properties;
    this.clock = clock;
  }

  public java.util.List<JobResponse> getJobResponseList() {
    return search(JobSearch.all(), properties.getJobResponseListLimit()).rows();
  }

  public JobPage search(JobSearch search, int size) {
    List<Object> params = new ArrayList<>();
    StringBuilder where = new StringBuilder("d.deliverydate>=?");
    params.add(LocalDateTime.now(clock).minusDays(14));
    add(where, params, "d.organisation", search.organisation(), false);
    add(where, params, "d.theme", search.theme(), true);
    add(where, params, "d.operat", search.operat(), true);
    add(where, params, "d.execution_status", search.status(), false);
    add(where, params, "d.validation_status", search.validationStatus(), false);
    int count =
        jdbc
            .queryForObject(
                queueCte() + " SELECT count(*) FROM status d WHERE " + where,
                Integer.class,
                params.toArray());
    String order =
        switch (search.sort()) {
          case "date-asc" -> "d.deliverydate ASC,d.t_id ASC";
          case "theme" -> "d.theme ASC,d.t_id DESC";
          case "operat" -> "d.operat ASC,d.t_id DESC";
          default -> "d.deliverydate DESC,d.t_id DESC";
        };
    int pageSize = Math.max(1, Math.min(size, properties.getJobResponseListLimit()));
    int page = search.page();
    params.add(pageSize);
    params.add((long) page * pageSize);
    var rows =
        jdbc
            .queryForList(
                queueCte() + " SELECT d.*,q.queueposition FROM status d LEFT JOIN queued q USING (jobid) WHERE "
                    + where
                    + " ORDER BY "
                    + order
                    + " LIMIT ? OFFSET ?",
                params.toArray());
    return new JobPage(rows.stream().map(this::response).toList(), count, page, pageSize);
  }

  public JobResponse getJobResponseById(String id) {
    var rows =
        jdbc
            .queryForList(
                queueCte() + " SELECT d.*,q.queueposition FROM status d LEFT JOIN queued q USING (jobid) WHERE d.jobid=?",
                id);
    return rows.isEmpty() ? null : response(rows.getFirst());
  }

  private String queueCte() {
    return """
        WITH status AS NOT MATERIALIZED (
          SELECT d.*, CASE
            WHEN processing_status IN ('PENDING','PUBLISHING','FAILED','PUBLICATION_FAILED')
              AND j.state IN ('ENQUEUED','PROCESSING','SCHEDULED') THEN j.state
            WHEN processing_status='PENDING' THEN COALESCE(j.state,'ENQUEUED')
            WHEN processing_status='PUBLISHING' THEN COALESCE(j.state,'ENQUEUED')
            WHEN processing_status IN ('FAILED','PUBLICATION_FAILED') THEN 'FAILED'
            ELSE 'SUCCEEDED'
          END AS execution_status,
          CASE WHEN isvalid THEN 'DONE' WHEN NOT isvalid THEN 'FAILED' END AS validation_status,
          CASE processing_status
            WHEN 'PENDING' THEN 'WAITING'
            WHEN 'INVALID' THEN 'SKIPPED'
            WHEN 'PUBLICATION_FAILED' THEN 'FAILED'
            WHEN 'PUBLISHING' THEN 'PENDING'
            ELSE processing_status
          END AS publication_status
          FROM %s.deliveries_delivery d
          LEFT JOIN %s.jobrunr_jobs j ON j.id=d.jobid::char(36)
        ), queued AS (
          SELECT jobid, row_number() OVER (ORDER BY deliverydate,t_id) AS queueposition
          FROM status WHERE execution_status='ENQUEUED'
        )
        """.formatted(properties.logSchema(), properties.jobSchema());
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
    LocalDateTime created = time(row, "deliverydate"),
        updated = time(row, "updated_at");
    String validation = (String) row.get("validation_status");
    var finished = time(row, "finished_at");
    boolean expired = properties.artifactsExpired((Boolean) row.get("artifacts_expired"), finished, clock);
    return new JobResponse(
        id, created, updated == null ? created : updated,
        (String) row.get("execution_status"),
        row.get("queueposition") instanceof Number position ? position.longValue() : null,
        (String) row.get("operat"), (String) row.get("theme"), (String) row.get("organisation"),
        expired ? "Log retention expired" : null, validation,
        finished == null ? null : properties.publicUrl("/api/logs/" + id),
        (String) row.get("publication_status"), (String) row.get("notification_status"));
  }

  private static LocalDateTime time(Map<String, Object> row, String column) {
    return row.get(column) instanceof java.sql.Timestamp value ? value.toLocalDateTime() : null;
  }
}
