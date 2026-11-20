package ch.so.agi.datahub.e2e;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import ch.so.agi.datahub.PostgresSupport;
import ch.so.agi.datahub.auth.ApiKeyFormat;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.jobrunr.storage.sql.common.DatabaseMigrationsProvider;
import org.jobrunr.storage.sql.common.migrations.SqlMigration;
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.BindMode;
import org.testcontainers.utility.MountableFile;
import org.junit.jupiter.api.io.TempDir;

class ProductionContainerTest extends PostgresSupport {
  @Test
  void generatedGdiSetupCreatesSchemasAndMigrationJournal() throws Exception {
    var setup = Path.of("sql/setup_gdi.sql");
    assertThat(Files.readString(setup)).endsWith(Files.readString(Path.of("sql/grants.sql")) + "\nCOMMIT;\n");
    var role = DATABASE.execInContainer("psql", "-X", "-v", "ON_ERROR_STOP=1",
        "-U", DATABASE.getUsername(), "-d", DATABASE.getDatabaseName(), "-c", "CREATE ROLE datahub");
    assertThat(role.getExitCode()).withFailMessage(role.getStderr()).isZero();
    DATABASE.copyFileToContainer(MountableFile.forHostPath(setup), "/tmp/gdi-setup.sql");
    var install = DATABASE.execInContainer("psql", "-X", "-U", DATABASE.getUsername(),
        "-d", DATABASE.getDatabaseName(), "-f", "/tmp/gdi-setup.sql");
    assertThat(install.getExitCode()).withFailMessage(install.getStderr()).isZero();
    var journal = DATABASE.execInContainer("psql", "-XAt", "-U", DATABASE.getUsername(),
        "-d", DATABASE.getDatabaseName(), "-c",
        "SELECT script FROM agi_datahub_jobrunr_v1.jobrunr_migrations ORDER BY script");
    assertThat(journal.getExitCode()).withFailMessage(journal.getStderr()).isZero();
    var migrations = new DatabaseMigrationsProvider(PostgresStorageProvider.class).getMigrations()
        .map(SqlMigration::getFileName).filter(name -> name.endsWith(".sql")).sorted().toList();
    assertThat(journal.getStdout().lines().toList()).isEqualTo(migrations);

    // PostgresSupport created test_config and test_log from the current ili2pg output.
    var shape = DATABASE.execInContainer("psql", "-XAt", "-U", DATABASE.getUsername(),
        "-d", DATABASE.getDatabaseName(), "-c", """
        WITH setup AS (
          SELECT replace(replace(table_schema, 'agi_datahub_', ''), '_v1', '') AS model,
              table_name, column_name, ordinal_position, data_type, udt_name, character_maximum_length,
              numeric_precision, numeric_scale, datetime_precision, is_nullable
          FROM information_schema.columns WHERE table_schema IN ('agi_datahub_config_v1', 'agi_datahub_log_v1')
        ), generated AS (
          SELECT replace(table_schema, 'test_', '') AS model,
              table_name, column_name, ordinal_position, data_type, udt_name, character_maximum_length,
              numeric_precision, numeric_scale, datetime_precision, is_nullable
          FROM information_schema.columns WHERE table_schema IN ('test_config', 'test_log')
        )
        SELECT NOT EXISTS (
          (SELECT * FROM setup EXCEPT SELECT * FROM generated)
          UNION ALL
          (SELECT * FROM generated EXCEPT SELECT * FROM setup)
        )
        AND EXISTS (
          SELECT FROM pg_index
          WHERE indexrelid = to_regclass('agi_datahub_config_v1.core_operat_theme_name_key')
          AND indisunique
        )
        AND has_table_privilege('datahub', 'agi_datahub_jobrunr_v1.jobrunr_jobs', 'INSERT')
        AND has_sequence_privilege('datahub', 'agi_datahub_log_v1.t_ili2db_seq', 'USAGE')
        AND has_table_privilege('datahub_read', 'agi_datahub_config_v1.core_apikey', 'SELECT')
        AND NOT has_table_privilege('datahub_read', 'agi_datahub_config_v1.core_apikey', 'INSERT')
        AND has_table_privilege('datahub_write', 'agi_datahub_config_v1.core_theme', 'UPDATE')
        AND NOT has_table_privilege('datahub_write', 'agi_datahub_log_v1.deliveries_delivery', 'UPDATE')
        """);
    assertThat(shape.getExitCode()).withFailMessage(shape.getStderr()).isZero();
    assertThat(shape.getStdout().trim()).isEqualTo("t");
  }

  @Test
  void productionImageRunsWithRandomUidPrecompiledTemplatesAndRealValidation(@TempDir Path secrets) throws Exception {
    var jdbc = initialize("container");
    long org =
        jdbc.queryForObject(
            "INSERT INTO container_config.core_organisation(aname,arole,email)"
                + " VALUES('Container','USER','container@example.test') RETURNING t_id",
            Long.class);
    long theme =
        jdbc.queryForObject(
            "INSERT INTO container_config.core_theme(aname,expected_models)"
                + " VALUES('Demo','DatahubDemo') RETURNING t_id",
            Long.class);
    jdbc.update(
        "INSERT INTO container_config.core_operat(aname,organisation_r,theme_r)"
            + " VALUES('sample',?,?),('Sample',?,?)",
        org, theme, org, theme);
    var id = UUID.randomUUID();
    var secret = UUID.randomUUID();
    String key = ApiKeyFormat.buildApiKey(id, secret);
    jdbc.update(
        "INSERT INTO container_config.core_apikey(apikey,organisation_r,createdat)"
            + " VALUES(?,?,CURRENT_TIMESTAMP)",
        ApiKeyFormat.buildStoredValue(id, new BCryptPasswordEncoder().encode(secret.toString())),
        org);
    jdbc.execute("CREATE USER datahub_container PASSWORD 'synthetic-password'");
    jdbc.execute("GRANT USAGE ON SCHEMA container_config, container_log, container_jobs TO datahub_container");
    jdbc.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA"
        + " container_config, container_log, container_jobs TO datahub_container");
    jdbc.execute("GRANT USAGE ON ALL SEQUENCES IN SCHEMA"
        + " container_config, container_log, container_jobs TO datahub_container");
    String bootstrap = ApiKeyFormat.buildApiKey(UUID.randomUUID(), UUID.randomUUID());
    Path bootstrapFile = Files.writeString(secrets.resolve("admin-key"), bootstrap);
    Files.setPosixFilePermissions(bootstrapFile, java.nio.file.attribute.PosixFilePermissions.fromString("r--r--r--"));
    int uid = java.util.concurrent.ThreadLocalRandom.current().nextInt(30000, 60000);
    try (var mail = new GenericContainer<>("axllent/mailpit:v1.31.4").withExposedPorts(1025)
            .withNetwork(NETWORK).withNetworkAliases("mail");
        var app = new GenericContainer<>(System.getProperty("app.productionImage"))
            .withNetwork(NETWORK)
            .withExposedPorts(8080)
            .withCreateContainerCmdModifier(cmd -> {
              cmd.withUser(uid + ":0");
              cmd.getHostConfig().withReadonlyRootfs(true).withTmpFs(Map.of(
                  "/tmp", "rw,mode=1777",
                  "/work", "rw,mode=2775,gid=0",
                  "/datahub", "rw,mode=2775,gid=0"));
            })
            .withEnv("DBURL", "jdbc:postgresql://database:5432/" + DATABASE.getDatabaseName())
            .withEnv("DBUSR", "datahub_container")
            .withEnv("DBPWD", "synthetic-password")
            .withEnv("CONFIG_DB_SCHEMA", "container_config")
            .withEnv("LOG_DB_SCHEMA", "container_log")
            .withEnv("JOBRUNR_DB_SCHEMA", "container_jobs.")
            .withEnv("PREFERRED_ILI_REPO", "/models")
            .withEnv("APP_DEFAULTILIREPOSITORIES", "false")
            .withEnv("MAIL_ENABLED", "true")
            .withEnv("MAIL_HOST", "mail")
            .withEnv("MAIL_PORT", "1025")
            .withEnv("MAIL_SMTP_AUTH", "false")
            .withEnv("MAIL_SMTP_STARTTLS", "false")
            .withEnv("PUBLIC_BASE_URL", "http://example.test/datahub")
            .withFileSystemBind(Path.of("dev/models").toAbsolutePath().toString(), "/models", BindMode.READ_ONLY)
            .withFileSystemBind(bootstrapFile.toRealPath().toString(), "/bootstrap-key", BindMode.READ_ONLY)
            .withEnv("ADMIN_KEY_FILE", "/bootstrap-key")
            .waitingFor(
                Wait.forHttp("/actuator/health")
                    .forPort(8080)
                    .withStartupTimeout(Duration.ofSeconds(60)))) {
      mail.start();
      app.start();
      String base = "http://" + app.getHost() + ":" + app.getMappedPort(8080);
      var client = HttpClient.newHttpClient();
      var page =
          client.send(
              HttpRequest.newBuilder(URI.create(base + "/jobs")).GET().build(),
              HttpResponse.BodyHandlers.ofString());
      assertThat(page.statusCode()).withFailMessage(page.body()).isEqualTo(200);
      assertThat(page.body()).contains("Übernahme", "/css/datahub.css", "/js/htmx.min.js",
          "<option value=\"date-desc\" selected", "<option value=\"operat\">")
          .doesNotContain("selected=\"false\"");
      var admin = client.send(HttpRequest.newBuilder(URI.create(base + "/api/files"))
          .header("X-API-KEY", bootstrap).GET().build(), HttpResponse.BodyHandlers.ofString());
      assertThat(admin.statusCode()).isEqualTo(200);
      String boundary = "container-datahub";
      String body =
          "--"
              + boundary
              + "\r\nContent-Disposition: form-data; name=\"theme\"\r\n\r\nDemo\r\n--"
              + boundary
              + "\r\nContent-Disposition: form-data; name=\"operat\"\r\n\r\nsample\r\n--"
              + boundary
              + "\r\n"
              + "Content-Disposition: form-data; name=\"file\"; filename=\"input.xtf\"\r\n"
              + "Content-Type: application/octet-stream\r\n\r\n"
              + Files.readString(Path.of("dev/valid.xtf"))
              + "\r\n--"
              + boundary
              + "--\r\n";
      for (String operat : List.of("sample", "Sample")) {
        var response = client.send(
            HttpRequest.newBuilder(URI.create(base + "/api/deliveries"))
                .header("X-API-KEY", key)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body.replace("\r\nsample\r\n",
                    "\r\n" + operat + "\r\n")))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(202);
      }
      await()
          .atMost(Duration.ofSeconds(50))
          .untilAsserted(
              () ->
                  assertThat(
                          jdbc.queryForObject(
                              "SELECT count(*) FROM container_log.deliveries_delivery WHERE"
                                  + " processing_status='DELIVERED' AND"
                                  + " notification_status='SENT'",
                              Integer.class))
                      .isEqualTo(2));
      var index = client.send(HttpRequest.newBuilder(URI.create(base + "/ilidata.xml")).GET().build(),
          HttpResponse.BodyHandlers.ofString());
      assertThat(index.body()).contains("sample.Demo", "Sample.Demo");
      assertThat(
              app.execInContainer(
                      "sh",
                      "-c",
                      "test -s /datahub/Demo/sample.xtf && test -s /datahub/Demo/sample.xtf.log"
                          + " && test -s /datahub/Demo/Sample.xtf && test -s /datahub/Demo/Sample.xtf.log"
                          + " && find /datahub/Demo -group 0 -perm -0020 | grep -q ."
                          + " && find /work -mindepth 1 -maxdepth 1 -type d -group 0 -perm -0020 | grep -q .")
                  .getExitCode())
          .isZero();
      assertThat(app.execInContainer("java", "-version").getStderr()).contains("version \"25");
      assertThat(app.execInContainer("id", "-u").getStdout().strip()).isEqualTo(Integer.toString(uid));
      // EXPOSE 8443 may be inherited from UBI; only port 8080 is published by the service.
      assertThat(app.getContainerInfo().getConfig().getExposedPorts())
          .contains(com.github.dockerjava.api.model.ExposedPort.tcp(8080));
      assertThat(app.execInContainer("cat", "/proc/1/cmdline").getStdout())
          .contains("-XX:-UseBASE64Intrinsics");
      assertThat(app.execInContainer("sh", "-c", "test ! -w /opt/app && test ! -w /opt/app/application.jar")
          .getExitCode()).isZero();
      assertThat(app.execInContainer("stat", "-c", "%a", "/opt/app/application.jar").getStdout().strip())
          .isEqualTo("444");
      assertThat(app.getLogs()).doesNotContain(key, bootstrap, "Template not found", "Using generated security password");
    }
  }
}
