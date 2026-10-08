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
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

class ProductionContainerTest extends PostgresSupport {
  @Test
  void productionImageRunsWithRandomUidPrecompiledTemplatesAndRealValidation() throws Exception {
    Flyway.configure()
        .dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
        .locations("filesystem:sql/migrations")
        .schemas("container_migration")
        .placeholders(
            Map.of(
                "configSchema",
                "container_config",
                "logSchema",
                "container_log",
                "jobSchema",
                "container_jobs"))
        .load()
        .migrate();
    var jdbc =
        new JdbcTemplate(
            new DriverManagerDataSource(
                DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword()));
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
            + " VALUES('sample',?,?)",
        org,
        theme);
    var id = UUID.randomUUID();
    var secret = UUID.randomUUID();
    String key = ApiKeyFormat.buildApiKey(id, secret);
    jdbc.update(
        "INSERT INTO container_config.core_apikey(apikey,organisation_r,createdat)"
            + " VALUES(?,?,CURRENT_TIMESTAMP)",
        ApiKeyFormat.buildStoredValue(id, new BCryptPasswordEncoder().encode(secret.toString())),
        org);
    jdbc.execute("CREATE USER datahub_container PASSWORD 'synthetic-password' IN ROLE datahub");
    var image =
        new ImageFromDockerfile()
            .withFileFromPath("Dockerfile", Path.of("docker/image/Dockerfile.ubi-layered"))
            .withFileFromPath(
                "tmp/datahub-test-exec.jar", Path.of(System.getProperty("app.productionJar")));
    try (var app =
        new GenericContainer<>(image)
            .withNetwork(NETWORK)
            .withExposedPorts(8080)
            .withCreateContainerCmdModifier(cmd -> cmd.withUser("34567:0"))
            .withEnv("DBURL", "jdbc:postgresql://database:5432/" + DATABASE.getDatabaseName())
            .withEnv("DBUSR", "datahub_container")
            .withEnv("DBPWD", "synthetic-password")
            .withEnv("CONFIG_DB_SCHEMA", "container_config")
            .withEnv("LOG_DB_SCHEMA", "container_log")
            .withEnv("JOBRUNR_DB_SCHEMA", "container_jobs.")
            .withEnv("ADMIN_ACCOUNT_INIT", "false")
            .withEnv("WORK_DIRECTORY", "/work")
            .withEnv("TARGET_DIRECTORY", "/datahub")
            .withEnv("PREFERRED_ILI_REPO", "/models")
            .withEnv("MAIL_ENABLED", "true")
            .withEnv("MAIL_HOST", "mail")
            .withEnv("MAIL_PORT", "1025")
            .withEnv("MAIL_SMTP_AUTH", "false")
            .withEnv("MAIL_SMTP_STARTTLS", "false")
            .withEnv("PUBLIC_BASE_URL", "http://example.test/datahub")
            .withCopyToContainer(MountableFile.forHostPath(Path.of("dev/models")), "/models")
            .waitingFor(
                Wait.forHttp("/actuator/health")
                    .forPort(8080)
                    .withStartupTimeout(Duration.ofSeconds(60)))) {
      app.start();
      String base = "http://" + app.getHost() + ":" + app.getMappedPort(8080);
      var client = HttpClient.newHttpClient();
      var page =
          client.send(
              HttpRequest.newBuilder(URI.create(base + "/jobs")).GET().build(),
              HttpResponse.BodyHandlers.ofString());
      assertThat(page.statusCode()).withFailMessage(page.body()).isEqualTo(200);
      assertThat(page.body()).contains("Übernahme", "/css/datahub.css", "/js/htmx.min.js");
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
      var response =
          client.send(
              HttpRequest.newBuilder(URI.create(base + "/api/deliveries"))
                  .header("X-API-KEY", key)
                  .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                  .POST(HttpRequest.BodyPublishers.ofString(body))
                  .build(),
              HttpResponse.BodyHandlers.ofString());
      assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(202);
      await()
          .atMost(Duration.ofSeconds(50))
          .untilAsserted(
              () ->
                  assertThat(
                          jdbc.queryForObject(
                              "SELECT count(*) FROM container_log.deliveries_delivery WHERE"
                                  + " publication_status='DELIVERED' AND"
                                  + " notification_status='SENT'",
                              Integer.class))
                      .isEqualTo(1));
      assertThat(
              app.execInContainer(
                      "sh",
                      "-c",
                      "id -u; test -s /datahub/Demo/sample.xtf; test -s"
                          + " /datahub/Demo/sample.xtf.log")
                  .getExitCode())
          .isZero();
      assertThat(app.getLogs()).doesNotContain(key, "Template not found");
    }
  }
}
