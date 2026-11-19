package ch.so.agi.datahub;

import static org.assertj.core.api.Assertions.*;

import ch.so.agi.datahub.auth.ApiKeyFormat;
import ch.so.agi.datahub.auth.ApiKeyHeaderAuthenticationService;
import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.service.JobResponseService;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;
import org.jobrunr.storage.sql.common.DatabaseCreator;
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.utility.MountableFile;

class SchemaUpgradeTest extends PostgresSupport {
  @Test
  void upgradesMainWithoutLosingHistoryOrActiveAndRevokedKeys() throws Exception {
    var source = new DriverManagerDataSource(
        DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
    var jdbc = new JdbcTemplate(source);
    jdbc.execute("CREATE ROLE datahub");
    DATABASE.copyFileToContainer(MountableFile.forClasspathResource("main-schema.sql"), "/tmp/main.sql");
    DATABASE.copyFileToContainer(MountableFile.forHostPath(Path.of("sql/upgrade-r0.sql")), "/tmp/upgrade.sql");
    assertThat(psql("/tmp/main.sql").getExitCode()).isZero();

    var encoder = new BCryptPasswordEncoder();
    var secret = UUID.randomUUID();
    var keyId = UUID.randomUUID();
    var revokedId = UUID.randomUUID();
    String keyHash = encoder.encode(secret.toString());
    jdbc.execute("""
        INSERT INTO agi_datahub_config_v1.core_organisation(t_id,aname,arole,email)
        VALUES (1000,'Supplier','USER','supplier@example.test')
        """);
    jdbc.update("""
        INSERT INTO agi_datahub_config_v1.core_apikey(apikey,createdat,organisation_r,revokedat)
        VALUES (?,CURRENT_TIMESTAMP,1000,NULL),(?,CURRENT_TIMESTAMP,1000,CURRENT_TIMESTAMP)
        """, ApiKeyFormat.buildStoredValue(keyId, keyHash), ApiKeyFormat.buildStoredValue(revokedId, keyHash));
    String delivery = UUID.randomUUID().toString();
    jdbc.update("""
        INSERT INTO agi_datahub_log_v1.deliveries_delivery(jobid,deliverydate,isvalid,isdelivered,organisation,theme,operat)
        VALUES (?,CURRENT_TIMESTAMP,true,true,'Supplier','Demo','sample')
        """, delivery);
    jdbc.update("""
        INSERT INTO agi_datahub_jobrunr_v1.jobrunr_jobs(id,version,jobAsJson,jobSignature,state,createdAt,updatedAt)
        VALUES (?,0,'{}','old job','ENQUEUED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
        """, delivery);

    // The modernization branch bound the old camel-case model attribute to this SQL column.
    jdbc.update("""
        INSERT INTO agi_datahub_config_v1.t_ili2db_attrname(iliname,sqlname,colowner)
        VALUES ('SO_AGI_Datahub_Config_20240403.Core.Theme.expectedModels','expected_models','core_theme')
        """);

    String oldAttempt = "SO_AGI_Datahub_Log_20240403.Deliveries.ValidationAttempt";
    jdbc.update("INSERT INTO agi_datahub_log_v1.t_ili2db_meta_attrs VALUES (?, 'obsolete', 'obsolete')",
        oldAttempt);
    jdbc.update("INSERT INTO agi_datahub_log_v1.t_ili2db_trafo VALUES (?, 'obsolete', 'obsolete')",
        oldAttempt);
    jdbc.update("INSERT INTO agi_datahub_log_v1.t_ili2db_inheritance(thisclass) VALUES (?)", oldAttempt);

    var refused = psql("/tmp/upgrade.sql");
    assertThat(refused.getExitCode()).isNotZero();
    assertThat(refused.getStderr()).contains("Drain active JobRunr jobs");
    jdbc.update("UPDATE agi_datahub_jobrunr_v1.jobrunr_jobs SET state='SUCCEEDED' WHERE id=?", delivery);
    var applied = psql("/tmp/upgrade.sql");
    assertThat(applied.getExitCode()).withFailMessage(applied.getStderr()).isZero();
    new DatabaseCreator(source, "agi_datahub_jobrunr_v1.", PostgresStorageProvider.class).runMigrations();

    for (String topic : new String[]{"Config", "Log"}) {
      String schema = "agi_datahub_" + topic.toLowerCase(Locale.ROOT) + "_v1";
      var cli = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
          "-cp", System.getProperty("app.ili2pgClasspath"), "ch.ehi.ili2pg.PgMain", "--schemaimport",
          "--dbhost", DATABASE.getHost(), "--dbport", DATABASE.getMappedPort(5432).toString(),
          "--dbdatabase", DATABASE.getDatabaseName(), "--dbusr", DATABASE.getUsername(),
          "--dbpwd", DATABASE.getPassword(), "--modeldir", "dev",
          "--models", "SO_AGI_Datahub_" + topic + "_20240403", "--dbschema", schema,
          "--nameByTopic", "--createFk", "--createFkIdx", "--createUnique",
          "--createImportTabs", "--createMetaInfo").redirectErrorStream(true).start();
      String output = new String(cli.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      assertThat(cli.waitFor()).withFailMessage(output).isZero();
      assertThat(jdbc.queryForObject("SELECT content FROM " + schema
          + ".t_ili2db_model WHERE filename='SO_AGI_Datahub_20240403.ili'", String.class))
          .contains("processing_status");
    }

    assertThat(jdbc.queryForObject("""
        SELECT sqlname FROM agi_datahub_config_v1.t_ili2db_attrname
        WHERE iliname='SO_AGI_Datahub_Config_20240403.Core.Theme.expected_models'
        """, String.class)).isEqualTo("expected_models");

    assertThat(jdbc.queryForObject("""
        SELECT count(*) FROM (
          SELECT ilielement FROM agi_datahub_log_v1.t_ili2db_meta_attrs WHERE ilielement=?
          UNION ALL SELECT iliname FROM agi_datahub_log_v1.t_ili2db_trafo WHERE iliname=?
          UNION ALL SELECT thisclass FROM agi_datahub_log_v1.t_ili2db_inheritance WHERE thisclass=?
        ) obsolete
        """, Integer.class, oldAttempt, oldAttempt, oldAttempt)).isZero();

    var properties = new AppProperties();
    var keys = new ApiKeyHeaderAuthenticationService(jdbc, properties, encoder, Clock.systemUTC());
    assertThat(keys.authenticate(ApiKeyFormat.buildApiKey(keyId, secret)).getName()).isEqualTo("Supplier");
    assertThatThrownBy(() -> keys.authenticate(ApiKeyFormat.buildApiKey(revokedId, secret)))
        .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
    assertThat(jdbc.queryForObject("SELECT apikey FROM agi_datahub_config_v1.core_apikey WHERE apikey LIKE ?",
        String.class, keyId + ":%")).isEqualTo(ApiKeyFormat.buildStoredValue(keyId, keyHash));

    jdbc.update("DELETE FROM agi_datahub_jobrunr_v1.jobrunr_jobs WHERE id=?", delivery);
    assertThat(new JobResponseService(jdbc, properties, Clock.systemUTC())
        .getJobResponseById(delivery).deliveryStatus()).isEqualTo("DELIVERED");
  }

  private org.testcontainers.containers.Container.ExecResult psql(String file) throws Exception {
    return DATABASE.execInContainer("psql", "-v", "ON_ERROR_STOP=1",
        "-U", DATABASE.getUsername(), "-d", DATABASE.getDatabaseName(), "-f", file);
  }
}
