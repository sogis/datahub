package ch.so.agi.datahub;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.so.agi.datahub.auth.*;
import ch.so.agi.datahub.config.*;
import ch.so.agi.datahub.model.*;
import ch.so.agi.datahub.service.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "jobrunr.background-job-server.enabled=false",
      "jobrunr.dashboard.enabled=false",
      "app.dispatchInterval=3600000",
      "app.followUpInterval=3600000"
    })
class DeliveryIntegrationTest extends PostgresSupport {
  @Value("${local.server.port}")
  int port;

  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordEncoder encoder;
  @Autowired ApiKeyHeaderAuthenticationService authentication;
  @Autowired KeyManagementService keys;
  @Autowired DeliverySubmissionService submission;
  @Autowired DeliveryRepository repository;
  @Autowired ValidationAttemptRunner runner;
  @Autowired ArtifactPublicationService publications;
  @Autowired NotificationService notifications;
  @Autowired ArtifactStore artifacts;
  @Autowired JobResponseService jobs;
  @Autowired AppProperties p;
  @Autowired Clock clock;
  @Autowired CayenneContexts contexts;
  @Autowired org.jobrunr.storage.StorageProvider storage;
  @Autowired DeliveryAuthorizationService authorization;
  String key;
  ApiKeyHeaderAuthenticationToken actor;
  long organisation, operat;
  final HttpClient client =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

  @BeforeEach
  void seed() throws Exception {
    jdbc.execute("TRUNCATE test_log.deliveries_delivery CASCADE");
    jdbc.execute("DELETE FROM test_config.core_apikey");
    jdbc.execute("DELETE FROM test_config.core_operat");
    jdbc.execute("DELETE FROM test_config.core_theme");
    jdbc.execute("DELETE FROM test_config.core_organisation");
    organisation =
        jdbc.queryForObject(
            "INSERT INTO test_config.core_organisation(aname,arole,email)"
                + " VALUES('Supplier','USER','supplier@example.test') RETURNING t_id",
            Long.class);
    long theme =
        jdbc.queryForObject(
            "INSERT INTO test_config.core_theme(aname,expected_models) VALUES('Demo','DatahubDemo')"
                + " RETURNING t_id",
            Long.class);
    operat =
        jdbc.queryForObject(
            "INSERT INTO test_config.core_operat(aname,theme_r,organisation_r) VALUES('sample',?,?)"
                + " RETURNING t_id",
            Long.class,
            theme,
            organisation);
    var id = UUID.randomUUID();
    var secret = UUID.randomUUID();
    key = ApiKeyFormat.buildApiKey(id, secret);
    jdbc.update(
        "INSERT INTO test_config.core_apikey(apikey,createdat,organisation_r,issuance_state)"
            + " VALUES(?,CURRENT_TIMESTAMP,?,'ACTIVE')",
        ApiKeyFormat.buildStoredValue(id, encoder.encode(secret.toString())),
        organisation);
    actor = authentication.authenticate(new ApiKeyHeaderAuthenticationToken(key));
    assertThat(actor.isAuthenticated()).isTrue();
    client.send(
        HttpRequest.newBuilder(URI.create(mailUrl() + "/api/v1/messages")).DELETE().build(),
        HttpResponse.BodyHandlers.discarding());
  }

  String mailUrl() {
    return "http://" + MAIL.getHost() + ":" + MAIL.getMappedPort(8025);
  }

  HttpResponse<String> get(String path, String token) throws Exception {
    var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    if (token != null) request.header("X-API-KEY", token);
    return client.send(request.GET().build(), HttpResponse.BodyHandlers.ofString());
  }

  HttpResponse<String> post(String query, String theme, String target, String extra, String token)
      throws Exception {
    String boundary = "datahub-integration-boundary";
    String body =
        part(boundary, "theme", theme)
            + part(boundary, "operat", target)
            + extra
            + "--"
            + boundary
            + "\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\"input.xtf\"\r\n"
            + "Content-Type: application/octet-stream\r\n\r\n"
            + Files.readString(Path.of("dev/valid.xtf"))
            + "\r\n--"
            + boundary
            + "--\r\n";
    var request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/deliveries" + query))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary);
    if (token != null) request.header("X-API-KEY", token);
    return client.send(
        request.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString());
  }

  static String part(String boundary, String name, String value) {
    return "--"
        + boundary
        + "\r\nContent-Disposition: form-data; name=\""
        + name
        + "\"\r\n\r\n"
        + value
        + "\r\n";
  }

  String submit(String fixture) throws Exception {
    return submission
        .submit(
            "Demo",
            "sample",
            new MockMultipartFile(
                "file",
                fixture,
                "application/octet-stream",
                Files.readAllBytes(Path.of("dev/" + fixture))),
            actor)
        .toString();
  }

  String attempt(String id) {
    return (String) repository.find(id).get("attempt_id");
  }

  void execute(String id) {
    assertThat(runner.run(id, attempt(id))).isTrue();
  }

  @Test
  void realHttpRejectsConflictsDuplicatesAndUnauthorizedTargets() throws Exception {
    assertThat(post("?theme=Other", "Demo", "sample", "", key).statusCode()).isEqualTo(400);
    assertThat(
            post("", "Demo", "sample", part("datahub-integration-boundary", "theme", "Other"), key)
                .statusCode())
        .isEqualTo(400);
    assertThat(post("", "Demo", "other", "", key).statusCode()).isEqualTo(403);
    assertThat(post("", "Demo", "sample", "", null).statusCode()).isEqualTo(401);
    assertThat(post("", "Demo", "sample", "", "invalid").statusCode()).isEqualTo(403);
    assertThat(
            jdbc.queryForObject("SELECT count(*) FROM test_log.deliveries_delivery", Integer.class))
        .isZero();
    var accepted = post("?theme=Demo", "Demo", "sample", "", key);
    assertThat(accepted.statusCode()).isEqualTo(202);
    assertThat(accepted.headers().firstValue("Operation-Location"))
        .hasValueSatisfying(s -> assertThat(s).startsWith("http://example.test/datahub/api/jobs/"));
    assertThat(
            jdbc.queryForObject("SELECT count(*) FROM test_log.validation_attempt", Integer.class))
        .isEqualTo(1);
  }

  @Test
  void validInvalidAndForeignModelsPublishOnlyValidDataAndMail() throws Exception {
    var id = submit("valid.xtf");
    execute(id);
    publications.publish(attempt(id));
    notifications.prepare();
    notifications.sendDue();
    var target = Path.of(p.getTargetDirectory(), "Demo", "sample.xtf");
    String original = Files.readString(target);
    assertThat(repository.find(id))
        .containsEntry("publication_status", "DELIVERED")
        .containsEntry("notification_status", "SENT");
    assertThat(get("/api/jobs/" + id, null).statusCode()).isEqualTo(200);
    assertThat(get("/api/logs/" + id, null).statusCode()).isEqualTo(200);
    var messages =
        client.send(
            HttpRequest.newBuilder(URI.create(mailUrl() + "/api/v1/messages")).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(messages.body())
        .contains("sender@example.test", "supplier@example.test", "Datahub DONE");
    for (String fixture : List.of("invalid.xtf", "other-model.xtf")) {
      var rejected = submit(fixture);
      execute(rejected);
      publications.publish(attempt(rejected));
      assertThat(repository.find(rejected)).containsEntry("validation_status", "FAILED");
      assertThat(Files.readString(target)).isEqualTo(original);
    }
    var index = get("/ilidata.xml", null);
    assertThat(index.body())
        .contains("<DatasetIdx16.DataIndex BID=\"b1\">", "DatahubDemo")
        .doesNotContain("IliRepository09.RepositoryIndex");
    Path xml = ROOT.resolve("index.xtf");
    Files.writeString(xml, index.body());
    var settings = new ch.ehi.basics.settings.Settings();
    settings.setValue(
        org.interlis2.validator.Validator.SETTING_ILIDIRS,
        Path.of("src/main/resources/ili").toAbsolutePath().toString());
    assertThat(org.interlis2.validator.Validator.runValidation(xml.toString(), settings)).isTrue();
  }

  @Test
  void olderCompletionDuplicateRunAndExpiredClaimCannotOverwriteNewerState() throws Exception {
    var older = submit("valid.xtf");
    var newer = submit("valid.xtf");
    execute(newer);
    publications.publish(attempt(newer));
    execute(older);
    publications.publish(attempt(older));
    assertThat(repository.find(older)).containsEntry("publication_status", "SUPERSEDED");
    assertThat(runner.run(newer, attempt(newer))).isTrue();
    assertThat(
            jdbc.queryForObject("SELECT count(*) FROM test_log.dataset_publication", Integer.class))
        .isEqualTo(1);
    var stale = submit("valid.xtf");
    long version = repository.claim(stale, attempt(stale)).orElseThrow();
    jdbc.update(
        "UPDATE test_log.validation_attempt SET heartbeat_at=? WHERE id=?",
        LocalDateTime.now(clock).minusMinutes(5),
        attempt(stale));
    repository.expireClaims();
    assertThat(repository.finish(stale, attempt(stale), version, true, "DatahubDemo", "test"))
        .isFalse();
    assertThat(repository.find(stale)).containsEntry("execution_status", "FAILED");
  }

  @Test
  void publicationFailureIsVisibleAndRetryRepairsWithoutValidation() throws Exception {
    var id = submit("valid.xtf");
    execute(id);
    var broken = new AppProperties();
    broken.setDbSchemaLog("test_log");
    broken.setTargetDirectory(ROOT.resolve("not-a-directory").toString());
    Files.writeString(Path.of(broken.getTargetDirectory()), "blocked");
    var badRepository = new DeliveryRepository(jdbc, repository.tx(), broken, clock);
    new ArtifactPublicationService(badRepository, artifacts).publish(attempt(id));
    assertThat(repository.find(id))
        .containsEntry("publication_status", "RETRY")
        .containsEntry("validation_status", "DONE");
    publications.publish(attempt(id));
    assertThat(repository.find(id)).containsEntry("publication_status", "DELIVERED");
  }

  @Test
  void keyIssuanceFailureLeavesNoActiveKeyAndAdministrativeEditsAreImmediate() {
    var failing = mock(EmailService.class);
    doThrow(new org.springframework.mail.MailSendException("unavailable"))
        .when(failing)
        .send(any(), any(), any());
    var service = new KeyManagementService(jdbc, repository.tx(), encoder, failing, p, clock);
    assertThatThrownBy(() -> service.issue(actor, null))
        .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM test_config.core_apikey WHERE issuance_state='ACTIVE'",
                Integer.class))
        .isEqualTo(1);
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM test_config.core_apikey WHERE issuance_state='FAILED'",
                Integer.class))
        .isEqualTo(1);
    jdbc.update(
        "UPDATE test_config.core_organisation SET arole='ADMIN' WHERE t_id=?", organisation);
    assertThat(
            authentication.authenticate(new ApiKeyHeaderAuthenticationToken(key)).getAuthorities())
        .extracting("authority")
        .containsExactly("ADMIN");
    jdbc.update(
        "UPDATE test_config.core_apikey SET dateofexpiry=?",
        LocalDateTime.now(clock).minusSeconds(1));
    assertThat(
            authentication.authenticate(new ApiKeyHeaderAuthenticationToken(key)).isAuthenticated())
        .isFalse();
  }

  @Test
  void independentContextsRollbackTogetherAndHistorySurvivesJobDeletion() throws Exception {
    assertThat(contexts.newContext()).isNotSameAs(contexts.newContext());
    assertThatThrownBy(
            () ->
                contexts.transaction(
                    ctx -> {
                      var d = ctx.newObject(ch.so.agi.datahub.cayenne.DeliveriesDelivery.class);
                      d.setJobid(UUID.randomUUID().toString());
                      d.setDeliverydate(LocalDateTime.now());
                      d.setOrganisation("test");
                      d.setTheme("test");
                      d.setOperat("test");
                      d.setPublicationTries(0);
                      d.setArtifactsExpired(false);
                      d.setExecutionStatus("ENQUEUED");
                      d.setPublicationStatus("WAITING");
                      d.setNotificationStatus("WAITING");
                      ctx.commitChanges();
                      throw new IllegalStateException("after first commit");
                    }))
        .isInstanceOf(RuntimeException.class)
        .hasStackTraceContaining("after first commit");
    assertThat(
            jdbc.queryForObject("SELECT count(*) FROM test_log.deliveries_delivery", Integer.class))
        .isZero();
    var id = submit("valid.xtf");
    execute(id);
    jdbc.execute("DELETE FROM test_jobs.jobrunr_jobs");
    assertThat(jobs.getJobResponseById(id)).isNotNull();
    jdbc.update(
        "UPDATE test_log.deliveries_delivery SET finished_at=? WHERE jobid=?",
        LocalDateTime.now(clock).minusDays(4),
        id);
    assertThat(get("/api/logs/" + id, null).statusCode()).isEqualTo(410);
  }

  @Test
  void filteringPaginationAndPublicRoutesAreConsistent() throws Exception {
    for (int i = 0; i < 27; i++) submit("valid.xtf");
    var page = jobs.search(new JobSearch(null, "De", null, "ENQUEUED", null, 0, "date-desc"), 25);
    assertThat(page.count()).isEqualTo(27);
    assertThat(page.rows()).hasSize(25);
    assertThat(page.hasNext()).isTrue();
    assertThat(
            jobs.search(new JobSearch(null, "De", null, "ENQUEUED", null, 1, "date-desc"), 25)
                .rows())
        .hasSize(2);
    jdbc.update(
        "UPDATE test_log.deliveries_delivery SET organisation='<script>alert(1)</script>' WHERE"
            + " t_id=(SELECT min(t_id) FROM test_log.deliveries_delivery)");
    var html = get("/jobs?sort=date-asc", null);
    assertThat(html.statusCode()).isEqualTo(200);
    assertThat(html.body()).contains("&lt;script&gt;").doesNotContain("<script>alert(1)</script>");
    assertThat(get("/css/datahub.css", null).body())
        .contains("--badge-valid-background: #c8e6c9", "font-family: system-ui");
    assertThat(get("/jobs/table?theme=De", null).headers().firstValue("HX-Push-Url"))
        .hasValue("/jobs?theme=De");
    assertThat(get("/web/jobs.xhtml", null).statusCode()).isEqualTo(302);
    assertThat(get("/ui/files", null).statusCode()).isEqualTo(401);
    assertThat(get("/ui/files", key).statusCode()).isEqualTo(403);
    assertThat(get("/new-unconfigured-endpoint", null).statusCode()).isIn(401, 403);
  }

  @Test
  void concurrentPublicationIsOrderedAndIdempotentAcrossInstances() throws Exception {
    var older = submit("valid.xtf");
    var newer = submit("valid.xtf");
    execute(older);
    execute(newer);
    var other =
        new ArtifactPublicationService(
            new DeliveryRepository(jdbc, repository.tx(), p, clock), artifacts);
    try (var threads = java.util.concurrent.Executors.newFixedThreadPool(3)) {
      var start = new java.util.concurrent.CountDownLatch(1);
      var tasks =
          List.of(
              threads.submit(
                  () -> {
                    start.await();
                    publications.publish(attempt(older));
                    return null;
                  }),
              threads.submit(
                  () -> {
                    start.await();
                    other.publish(attempt(newer));
                    return null;
                  }),
              threads.submit(
                  () -> {
                    start.await();
                    publications.publish(attempt(newer));
                    return null;
                  }));
      start.countDown();
      for (var task : tasks) task.get(10, java.util.concurrent.TimeUnit.SECONDS);
    }
    assertThat(
            jdbc.queryForObject(
                "SELECT delivery_id FROM test_log.dataset_publication WHERE operat_id=?",
                Long.class,
                operat))
        .isEqualTo(((Number) repository.find(newer).get("t_id")).longValue());
    assertThat(repository.find(newer)).containsEntry("publication_status", "DELIVERED");
    assertThat(
            jdbc.queryForObject(
                "SELECT model_metadata FROM test_log.validation_attempt WHERE id=?",
                String.class,
                attempt(newer)))
        .contains("DatahubDemo", "2026-10-08", "sha256", "MODEL DatahubDemo");
  }

  @Test
  void notificationRetriesAreBoundedAndDoNotRerunValidation() throws Exception {
    var id = submit("invalid.xtf");
    execute(id);
    notifications.prepare();
    var unavailable = mock(EmailService.class);
    doThrow(new org.springframework.mail.MailSendException("unavailable"))
        .when(unavailable)
        .send(any(), any(), any());
    var sender = new NotificationService(repository, unavailable);
    for (int failure = 1; failure <= 4; failure++) {
      jdbc.update(
          "UPDATE test_log.notification SET next_at=?", LocalDateTime.now(clock).minusSeconds(1));
      sender.sendDue();
      assertThat(repository.find(id))
          .containsEntry("notification_status", failure == 4 ? "FAILED" : "RETRY");
    }
    sender.sendDue();
    verify(unavailable, times(4)).send(any(), any(), any());
    assertThat(
            jdbc.queryForObject(
                "SELECT version FROM test_log.validation_attempt WHERE id=?",
                Long.class,
                attempt(id)))
        .isEqualTo(1);
    assertThat(repository.find(id)).containsEntry("validation_status", "FAILED");
  }

  @Test
  void keyMailIsDeliveredOnceAndDisabledMailRejectsBeforeInsert() throws Exception {
    var disabled = new AppProperties();
    disabled.setDbSchemaConfig("test_config");
    disabled.setMailEnabled(false);
    var service =
        new KeyManagementService(
            jdbc, repository.tx(), encoder, mock(EmailService.class), disabled, clock);
    assertThatThrownBy(() -> service.issue(actor, null))
        .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    assertThat(jdbc.queryForObject("SELECT count(*) FROM test_config.core_apikey", Integer.class))
        .isEqualTo(1);
    keys.issue(actor, null);
    var messages =
        client.send(
            HttpRequest.newBuilder(URI.create(mailUrl() + "/api/v1/messages")).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
    String messageId = mapper.readTree(messages.body()).get("messages").get(0).get("ID").asText();
    var message =
        client.send(
            HttpRequest.newBuilder(URI.create(mailUrl() + "/api/v1/message/" + messageId))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString());
    var text = mapper.readTree(message.body()).get("Text").asText().trim();
    assertThat(text).matches("[a-f0-9-]{36}\\.[a-f0-9-]{36}");
    assertThat(
            authentication
                .authenticate(new ApiKeyHeaderAuthenticationToken(text))
                .isAuthenticated())
        .isTrue();
    assertThat(
            jdbc.queryForObject(
                "SELECT count(*) FROM test_config.core_apikey WHERE issuance_state='ACTIVE'",
                Integer.class))
        .isEqualTo(2);
    assertThat(jdbc.queryForObject("SELECT count(*) FROM test_log.notification", Integer.class))
        .isZero();
    var replica = new ApiKeyHeaderAuthenticationService(jdbc, p, encoder, clock);
    assertThat(replica.authenticate(new ApiKeyHeaderAuthenticationToken(key)).isAuthenticated())
        .isTrue();
    assertThat(keys.revoke(actor, key)).isTrue();
    assertThat(
            authentication.authenticate(new ApiKeyHeaderAuthenticationToken(key)).isAuthenticated())
        .isFalse();
    assertThat(replica.authenticate(new ApiKeyHeaderAuthenticationToken(key)).isAuthenticated())
        .isFalse();
    assertThat(post("", "Demo", "sample", "", key).statusCode()).isEqualTo(403);
  }

  @Test
  void smtpTimeoutLeavesIssuedKeyInactive() throws Exception {
    try (var threads = java.util.concurrent.Executors.newSingleThreadExecutor();
        var server = new java.net.ServerSocket(0)) {
      var waiting =
          threads.submit(
              () -> {
                try (var socket = server.accept()) {
                  socket.getInputStream().read();
                }
                return null;
              });
      var sender = new org.springframework.mail.javamail.JavaMailSenderImpl();
      sender.setHost("localhost");
      sender.setPort(server.getLocalPort());
      sender.getJavaMailProperties().setProperty("mail.smtp.connectiontimeout", "5000");
      sender.getJavaMailProperties().setProperty("mail.smtp.timeout", "5000");
      sender.getJavaMailProperties().setProperty("mail.smtp.writetimeout", "5000");
      var service =
          new KeyManagementService(
              jdbc, repository.tx(), encoder, new EmailService(sender, p), p, clock);
      assertThatThrownBy(() -> service.issue(actor, null))
          .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
      waiting.get(10, java.util.concurrent.TimeUnit.SECONDS);
      assertThat(
              jdbc.queryForObject(
                  "SELECT count(*) FROM test_config.core_apikey WHERE issuance_state='ACTIVE'",
                  Integer.class))
          .isEqualTo(1);
      assertThat(
              jdbc.queryForObject(
                  "SELECT count(*) FROM test_config.core_apikey WHERE issuance_state='FAILED'",
                  Integer.class))
          .isEqualTo(1);
    }
  }

  @Test
  void cleanupUsesCompletionAndPreservesActivePendingAndPublishedArtifacts() throws Exception {
    var obsolete = submit("invalid.xtf");
    execute(obsolete);
    jdbc.update(
        "UPDATE test_log.deliveries_delivery SET notification_status='DISABLED' WHERE jobid=?",
        obsolete);
    var current = submit("valid.xtf");
    execute(current);
    publications.publish(attempt(current));
    var pending = submit("valid.xtf");
    execute(pending);
    var active = submit("valid.xtf");
    repository.claim(active, attempt(active));
    var orphan = UUID.randomUUID();
    artifacts.saveInput(orphan, new java.io.ByteArrayInputStream("orphan".getBytes()));
    Clock later = Clock.offset(clock, Duration.ofDays(4));
    var cleaner =
        new CleanerService(new DeliveryRepository(jdbc, repository.tx(), p, later), artifacts);
    cleaner.cleanUp();
    assertThat(repository.find(obsolete)).containsEntry("artifacts_expired", true);
    assertThat(artifacts.input(UUID.fromString(obsolete))).doesNotExist();
    assertThat(artifacts.input(orphan)).doesNotExist();
    var target = Path.of(p.getTargetDirectory(), "Demo");
    var oldStage = Files.createTempFile(target, ".publication-" + attempt(current) + "-", ".tmp");
    var pendingStage =
        Files.createTempFile(target, ".publication-" + attempt(pending) + "-", ".tmp");
    cleaner.cleanUp();
    assertThat(oldStage).doesNotExist();
    assertThat(pendingStage).exists();
    for (var id : List.of(current, pending, active)) {
      assertThat(artifacts.input(UUID.fromString(id))).exists();
      assertThat(repository.find(id)).containsEntry("artifacts_expired", false);
    }
  }

  @Test
  void jobrunrEightReadsPreviouslySerializedJobs() throws Exception {
    String id = "9ba5cd95-847e-40da-b292-01cad1fa9839";
    jdbc.update(
        "INSERT INTO"
            + " test_jobs.jobrunr_jobs(id,version,jobasjson,jobsignature,state,createdat,updatedat)"
            + " VALUES(?,3,?,'fixture','SUCCEEDED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) ON"
            + " CONFLICT(id) DO NOTHING",
        id,
        Files.readString(Path.of("src/integrationTest/resources/jobrunr-6-completed-job.json")));
    assertThat(storage.getJobById(UUID.fromString(id)).getState())
        .isEqualTo(org.jobrunr.jobs.states.StateName.SUCCEEDED);
  }

  @Test
  void failedDatabaseCommitLeavesAnOrphanButNoDeliveryOrDispatch() throws Exception {
    var failed = mock(CayenneContexts.class);
    when(failed.transaction(any()))
        .thenThrow(new IllegalStateException("simulated commit failure"));
    var service = new DeliverySubmissionService(authorization, failed, artifacts, p, clock);
    assertThatThrownBy(
            () ->
                service.submit(
                    "Demo",
                    "sample",
                    new MockMultipartFile("file", Files.readAllBytes(Path.of("dev/valid.xtf"))),
                    actor))
        .isInstanceOf(IllegalStateException.class);
    assertThat(
            jdbc.queryForObject("SELECT count(*) FROM test_log.deliveries_delivery", Integer.class))
        .isZero();
    assertThat(
            jdbc.queryForObject("SELECT count(*) FROM test_log.validation_attempt", Integer.class))
        .isZero();
  }

  @Test
  void publicationRepairsACompletedLogMoveAfterTransferMoveFailure() throws Exception {
    var id = submit("valid.xtf");
    execute(id);
    var transfer = Path.of(p.getTargetDirectory(), "Demo", "sample.xtf");
    Files.createDirectories(transfer.getParent());
    Files.deleteIfExists(transfer);
    Files.createDirectory(transfer);
    publications.publish(attempt(id));
    assertThat(repository.find(id)).containsEntry("publication_status", "RETRY");
    assertThat(transfer.resolveSibling("sample.xtf.log")).isRegularFile();
    Files.delete(transfer);
    publications.publish(attempt(id));
    assertThat(repository.find(id)).containsEntry("publication_status", "DELIVERED");
    assertThat(Files.readString(transfer)).isEqualTo(Files.readString(Path.of("dev/valid.xtf")));
  }

  @Test
  void artifactFailureImmediatelyClosesAttemptAsTechnicalFailure() throws Exception {
    var id = submit("valid.xtf");
    var broken = mock(ArtifactStore.class, org.mockito.AdditionalAnswers.delegatesTo(artifacts));
    doThrow(new java.io.IOException("storage unavailable"))
        .when(broken)
        .saveResult(any(), any(), anyLong(), any());
    try (var isolated =
        new ValidationAttemptRunner(repository, broken, new IliValidatorService(), p)) {
      assertThat(isolated.run(id, attempt(id))).isFalse();
    }
    assertThat(repository.find(id))
        .containsEntry("execution_status", "FAILED")
        .containsEntry("publication_status", "NOT_APPLICABLE");
  }

  @Test
  void oldAttemptExpiryCannotFailAReplacementAttempt() throws Exception {
    var id = submit("valid.xtf");
    String old = attempt(id);
    repository.claim(id, old);
    var replacement = UUID.randomUUID().toString();
    jdbc.update(
        "INSERT INTO test_log.validation_attempt(id,delivery_id,created_at) VALUES(?,?,?)",
        replacement,
        repository.find(id).get("t_id"),
        LocalDateTime.now(clock));
    jdbc.update(
        "UPDATE test_log.deliveries_delivery SET attempt_id=?,execution_status='ENQUEUED' WHERE"
            + " jobid=?",
        replacement,
        id);
    jdbc.update(
        "UPDATE test_log.validation_attempt SET heartbeat_at=? WHERE id=?",
        LocalDateTime.now(clock).minusMinutes(5),
        old);
    repository.expireClaims();
    assertThat(repository.find(id)).containsEntry("execution_status", "ENQUEUED");
    assertThat(runner.run(id, replacement)).isTrue();
    assertThat(repository.find(id)).containsEntry("execution_status", "SUCCEEDED");
  }

  @Test
  void cayenneAndDeliveryAcceptThemeNamesBeyondTheOldTwentyCharacterMapping() throws Exception {
    String name = "Datahub_theme_with_a_name_longer_than_twenty_characters";
    long theme =
        contexts.transaction(
            ctx -> {
              var value = ctx.newObject(ch.so.agi.datahub.cayenne.CoreTheme.class);
              value.setAname(name);
              value.setExpectedModels("DatahubDemo");
              ctx.commitChanges();
              return org.apache.cayenne.Cayenne.longPKForObject(value);
            });
    jdbc.update("UPDATE test_config.core_operat SET theme_r=? WHERE t_id=?", theme, operat);
    var id =
        submission
            .submit(
                new DeliverySubmissionService.Command(
                    name,
                    "sample",
                    new MockMultipartFile("file", Files.readAllBytes(Path.of("dev/valid.xtf")))),
                actor)
            .toString();
    execute(id);
    publications.publish(attempt(id));
    assertThat(repository.find(id))
        .containsEntry("theme", name)
        .containsEntry("publication_status", "DELIVERED");
  }
}
