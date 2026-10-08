package ch.so.agi.datahub;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.*;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SchemaMigrationTest extends PostgresSupport {
  Flyway flyway(String prefix, String target) {
    return Flyway.configure()
        .dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
        .locations("filesystem:sql/migrations")
        .schemas(prefix + "_migration")
        .target(target)
        .placeholders(
            Map.of(
                "configSchema",
                prefix + "_config",
                "logSchema",
                prefix + "_log",
                "jobSchema",
                prefix + "_jobs"))
        .load();
  }

  JdbcTemplate jdbc() {
    return new JdbcTemplate(
        new DriverManagerDataSource(
            DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword()));
  }

  @Test
  void existingInstallationPreservesIdentityKeysAndCompletedHistory() throws Exception {
    flyway("legacy", "1").migrate();
    var jdbc = jdbc();
    String job = "9ba5cd95-847e-40da-b292-01cad1fa9839";
    jdbc.update(
        "INSERT INTO"
            + " legacy_log.deliveries_delivery(jobid,deliverydate,organisation,theme,operat,isvalid,isdelivered)"
            + " VALUES(?,CURRENT_TIMESTAMP,'Legacy','Demo','sample',true,true)",
        job);
    jdbc.update(
        "INSERT INTO legacy_config.core_organisation(t_id,aname,arole,email)"
            + " VALUES(99,'Legacy','USER','legacy@example.test')");
    jdbc.update(
        "INSERT INTO legacy_config.core_apikey(apikey,organisation_r,createdat)"
            + " VALUES('synthetic-hash',99,CURRENT_TIMESTAMP)");
    jdbc.update(
        "INSERT INTO"
            + " legacy_jobs.jobrunr_jobs(id,version,jobasjson,jobsignature,state,createdat,updatedat)"
            + " VALUES(?,3,?,'fixture','SUCCEEDED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
        job,
        Files.readString(Path.of("src/integrationTest/resources/jobrunr-6-completed-job.json")));
    flyway("legacy", "latest").migrate();
    assertThat(jdbc.queryForMap("SELECT * FROM legacy_log.deliveries_delivery WHERE jobid=?", job))
        .containsEntry("execution_status", "SUCCEEDED")
        .containsEntry("validation_status", "DONE")
        .containsEntry("publication_status", "LEGACY")
        .containsEntry("attempt_id", null);
    assertThat(
            jdbc.queryForObject(
                "SELECT issuance_state FROM legacy_config.core_apikey", String.class))
        .isEqualTo("ACTIVE");
    jdbc.execute("DELETE FROM legacy_jobs.jobrunr_jobs");
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM legacy_log.deliveries_delivery", Integer.class))
        .isEqualTo(1);
  }

  @Test
  void duplicateOperatsAbortWithoutPartiallyApplyingMigration() {
    flyway("duplicate", "1").migrate();
    var jdbc = jdbc();
    jdbc.execute(
        "INSERT INTO duplicate_config.core_operat(aname,organisation_r,theme_r)"
            + " VALUES('duplicate',1,1),('duplicate',1,1)");
    assertThatThrownBy(() -> flyway("duplicate", "latest").migrate())
        .hasStackTraceContaining("Duplicate (theme_r, aname)");
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.columns WHERE"
                    + " table_schema='duplicate_config' AND table_name='core_apikey' AND"
                    + " column_name='issuance_state'",
                Integer.class))
        .isZero();
  }
}
