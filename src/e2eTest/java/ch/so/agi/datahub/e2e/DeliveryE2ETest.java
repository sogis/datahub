package ch.so.agi.datahub.e2e;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import ch.so.agi.datahub.PostgresSupport;
import ch.so.agi.datahub.auth.ApiKeyFormat;
import ch.so.agi.datahub.service.DeliveryRepository;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryE2ETest extends PostgresSupport {
  @Value("${local.server.port}")
  int port;

  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordEncoder encoder;
  @Autowired DeliveryRepository repository;
  @Autowired ch.so.agi.datahub.config.AppProperties properties;
  String key;

  @AfterEach
  void restoreDispatch() {
    properties.setDispatchEnabled(true);
  }

  @DynamicPropertySource
  static void worker(DynamicPropertyRegistry registry) {
    registry.add("jobrunr.background-job-server.enabled", () -> true);
    registry.add("jobrunr.background-job-server.poll-interval-in-seconds", () -> 5);
    registry.add("app.dispatchInterval", () -> 100);
    registry.add("app.followUpInterval", () -> 100);
  }

  @BeforeEach
  void seed() {
    jdbc.execute("TRUNCATE test_log.deliveries_delivery CASCADE");
    jdbc.execute("DELETE FROM test_config.core_apikey");
    jdbc.execute("DELETE FROM test_config.core_operat");
    jdbc.execute("DELETE FROM test_config.core_theme");
    jdbc.execute("DELETE FROM test_config.core_organisation");
    long org =
        jdbc.queryForObject(
            "INSERT INTO test_config.core_organisation(aname,arole,email)"
                + " VALUES('E2E','USER','e2e@example.test') RETURNING t_id",
            Long.class);
    long theme =
        jdbc.queryForObject(
            "INSERT INTO test_config.core_theme(aname,expected_models) VALUES('Demo','DatahubDemo')"
                + " RETURNING t_id",
            Long.class);
    jdbc.update(
        "INSERT INTO test_config.core_operat(aname,theme_r,organisation_r) VALUES('sample',?,?)",
        theme,
        org);
    var id = UUID.randomUUID();
    var secret = UUID.randomUUID();
    key = ApiKeyFormat.buildApiKey(id, secret);
    jdbc.update(
        "INSERT INTO test_config.core_apikey(apikey,createdat,organisation_r)"
            + " VALUES(?,CURRENT_TIMESTAMP,?)",
        ApiKeyFormat.buildStoredValue(id, encoder.encode(secret.toString())),
        org);
  }

  String submit(String fixture) throws Exception {
    String boundary = "e2e-datahub";
    String body = "";
    for (var field : Map.of("theme", "Demo", "operat", "sample").entrySet())
      body +=
          "--"
              + boundary
              + "\r\nContent-Disposition: form-data; name=\""
              + field.getKey()
              + "\"\r\n\r\n"
              + field.getValue()
              + "\r\n";
    body +=
        "--"
            + boundary
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\"input.xtf\"\r\n"
            + "Content-Type: application/octet-stream\r\n\r\n"
            + Files.readString(Path.of("dev/" + fixture))
            + "\r\n--"
            + boundary
            + "--\r\n";
    var response =
        HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/deliveries"))
                    .header("X-API-KEY", key)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build(),
                HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(202);
    var url = response.headers().firstValue("Operation-Location").orElseThrow();
    return url.substring(url.lastIndexOf('/') + 1);
  }

  @Test
  void realJobRunrValidationPublicationAndSmtpCompleteWithoutStub() throws Exception {
    var id = submit("valid.xtf");
    await()
        .atMost(Duration.ofSeconds(40))
        .untilAsserted(
            () ->
                assertThat(repository.find(id))
                    .containsEntry("publication_status", "DELIVERED")
                    .containsEntry("notification_status", "SENT"));
    var attempt = (String) repository.find(id).get("attempt_id");
    assertThat(attempt).isNotEqualTo(id);
    var job = jdbc.queryForMap("SELECT jobasjson FROM test_jobs.jobrunr_jobs WHERE id=?", attempt);
    assertThat((String) job.get("jobasjson"))
        .contains(id, attempt)
        .doesNotContain(key, "e2e@example.test");
    assertThat(ROOT.resolve("target/Demo/sample.xtf")).exists();
    assertThat(ROOT.resolve("target/Demo/sample.xtf.log")).exists();
    var rejected = submit("invalid.xtf");
    await()
        .atMost(Duration.ofSeconds(40))
        .untilAsserted(
            () ->
                assertThat(repository.find(rejected))
                    .containsEntry("validation_status", "FAILED")
                    .containsEntry("notification_status", "SENT"));
    assertThat(repository.find(rejected)).containsEntry("publication_status", "NOT_APPLICABLE");
  }

  @Test
  void technicalFailureIsFailedInDeliveryAndJobRunrWithoutRetry() throws Exception {
    properties.setDispatchEnabled(false);
    String id = submit("valid.xtf");
    String attempt = (String) repository.find(id).get("attempt_id");
    Files.delete(ROOT.resolve("work/datahub_" + id + "/input.xtf"));
    properties.setDispatchEnabled(true);
    await()
        .atMost(Duration.ofSeconds(40))
        .untilAsserted(
            () -> {
              assertThat(repository.find(id)).containsEntry("execution_status", "FAILED");
              assertThat(
                      jdbc.queryForObject(
                          "SELECT state FROM test_jobs.jobrunr_jobs WHERE id=?",
                          String.class,
                          attempt))
                  .isEqualTo("FAILED");
            });
    assertThat(
            jdbc.queryForObject(
                "SELECT version FROM test_log.validation_attempt WHERE id=?", Long.class, attempt))
        .isEqualTo(1);
  }

  @Test
  void standaloneWorkerHasNoWebServerAndDistinguishesTechnicalFailures() throws Exception {
    // Disable dispatch while the separate process owns the attempt.
    properties.setDispatchEnabled(false);
    for (String fixture : List.of("valid.xtf", "invalid.xtf", "technical")) {
      var id = submit(fixture.equals("technical") ? "valid.xtf" : fixture);
      var attempt = (String) repository.find(id).get("attempt_id");
      jdbc.update(
          "UPDATE test_log.validation_attempt SET dispatch_state='SUBMITTED' WHERE id=?", attempt);
      if (fixture.equals("technical"))
        Files.delete(ROOT.resolve("work/datahub_" + id + "/input.xtf"));
      int exit = runSingle(attempt);
      assertThat(exit).isEqualTo(fixture.equals("technical") ? 1 : 0);
      assertThat(repository.find(id))
          .containsEntry("execution_status", fixture.equals("technical") ? "FAILED" : "SUCCEEDED");
      if (fixture.equals("invalid.xtf"))
        assertThat(repository.find(id)).containsEntry("validation_status", "FAILED");
    }
  }

  int runSingle(String attempt) throws Exception {
    String classpath = System.getProperty("app.testRuntimeClasspath");
    assertThat(classpath).isNotBlank();
    Path output = ROOT.resolve("single-worker.log");
    var process =
        new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin/java").toString(),
                "-cp",
                classpath,
                "ch.so.agi.datahub.DatahubApplication",
                "--app.mode=worker-single",
                "--app.attempt-id=" + attempt,
                "--spring.datasource.url=" + DATABASE.getJdbcUrl(),
                "--spring.datasource.username=" + DATABASE.getUsername(),
                "--spring.datasource.password=" + DATABASE.getPassword(),
                "--app.db-schema-config=test_config",
                "--app.db-schema-log=test_log",
                "--app.db-schema-job=test_jobs",
                "--app.work-directory=" + ROOT.resolve("work"),
                "--app.target-directory=" + ROOT.resolve("target"),
                "--app.preferred-ili-repo=" + Path.of("dev/models").toAbsolutePath(),
                "--app.default-ili-repositories=false",
                "--app.admin-account-init=false",
                "--app.mail-enabled=false")
            .redirectErrorStream(true)
            .redirectOutput(output.toFile())
            .start();
    assertThat(process.waitFor(40, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
    assertThat(process.exitValue()).withFailMessage(Files.readString(output)).isIn(0, 1);
    assertThat(Files.readString(output)).doesNotContain("Tomcat started", "BackgroundJobServer (");
    return process.exitValue();
  }
}
