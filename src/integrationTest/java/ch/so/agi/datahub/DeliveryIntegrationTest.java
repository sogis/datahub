package ch.so.agi.datahub;

import static org.assertj.core.api.Assertions.*;

import ch.so.agi.datahub.auth.ApiKeyFormat;
import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.service.CleanerService;
import java.time.Clock;
import ch.so.agi.datahub.service.DeliveryFiles;
import ch.so.agi.datahub.service.DeliveryService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "jobrunr.background-job-server.enabled=false")
class DeliveryIntegrationTest extends PostgresSupport {
  private static final String BOUNDARY = "datahub-integration-boundary";
  private static final String DELIVERIES = "test_log.deliveries_delivery";
  private static final String JOBS = "test_jobs.jobrunr_jobs";
  private static final Path TARGET = ROOT.resolve("target/Demo/sample.xtf");
  private final HttpClient client = HttpClient.newHttpClient();

  @Value("${local.server.port}") int port;
  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordEncoder encoder;
  @Autowired DeliveryService deliveries;
  @Autowired DeliveryFiles files;
  @Autowired JsonMapper json;
  @Autowired AppProperties properties;
  @Autowired Clock clock;
  @Autowired org.springframework.transaction.support.TransactionTemplate transactions;
  String supplier;
  String admin;

  @BeforeEach
  void seed() throws IOException {
    jdbc.execute("TRUNCATE " + DELIVERIES + ", " + JOBS);
    jdbc.execute("TRUNCATE test_config.core_apikey, test_config.core_operat, "
        + "test_config.core_theme, test_config.core_organisation");
    jdbc.update("""
        INSERT INTO test_config.core_organisation(t_id,aname,arole,email)
        VALUES (1000,'Supplier','USER','supplier@example.test'),(1001,'Admin','ADMIN','admin@example.test')
        """);
    jdbc.update("""
        INSERT INTO test_config.core_theme(t_id,aname,expected_models) VALUES (1002,'Demo','DatahubDemo')
        """);
    jdbc.update("""
        INSERT INTO test_config.core_operat(aname,theme_r,organisation_r)
        VALUES ('sample',1002,1000),('other',1002,1001)
        """);
    supplier = createKey(1000);
    admin = createKey(1001);
    Files.deleteIfExists(TARGET);
    Files.deleteIfExists(TARGET.resolveSibling("sample.xtf.log"));
  }

  @Test
  void rejectsUnauthenticatedAndConflictingMultipartRequestsBeforeRegistration() throws Exception {
    String upload = upload("sample", valid());
    assertThat(post("/api/deliveries", upload, null).statusCode()).isEqualTo(401);
    assertThat(post("/api/deliveries", upload, "invalid").statusCode()).isEqualTo(401);
    assertThat(post("/api/deliveries?token=" + supplier, upload, null).statusCode()).isEqualTo(401);
    assertThat(send(HttpRequest.newBuilder(uri("/api/files"))
        .header("X-API-KEY", admin).header("X-API-KEY", admin)).statusCode()).isEqualTo(401);
    String expired = createKey(1000);
    jdbc.update("UPDATE test_config.core_apikey SET dateofexpiry=now()-interval '1 day' WHERE apikey LIKE ?",
        expired.substring(0, 36) + ":%");
    assertThat(post("/api/deliveries", upload, expired).statusCode()).isEqualTo(401);
    jdbc.execute("ALTER TABLE test_config.core_apikey RENAME TO unavailable");
    try {
      assertThat(post("/api/deliveries", upload, supplier).statusCode()).isEqualTo(500);
    } finally {
      jdbc.execute("ALTER TABLE test_config.unavailable RENAME TO core_apikey");
    }
    assertThat(post("/api/deliveries", upload("other", valid()), supplier).statusCode()).isEqualTo(403);
    assertThat(post("/api/deliveries?theme=Other", upload, supplier).statusCode()).isEqualTo(400);
    assertThat(post("/api/deliveries?file=unknown", upload, supplier).statusCode()).isEqualTo(400);
    assertThat(post("/api/deliveries?theme=Demo&theme=Demo", upload, supplier).statusCode()).isEqualTo(400);
    assertThat(post("/api/deliveries", part("theme", "Other") + upload, supplier).statusCode()).isEqualTo(400);
    assertThat(jdbc.queryForObject("SELECT count(*) FROM " + DELIVERIES, Integer.class)).isZero();
    assertThat(post("/api/deliveries?theme=Demo", upload, supplier).statusCode()).isEqualTo(202);
  }

  @Test
  void onlyAdminsIssueAndRevokeByPublicId() throws Exception {
    assertThat(post("/api/keys", part("organisation", "Supplier"), supplier).statusCode()).isEqualTo(403);
    var issued = post("/api/keys", part("organisation", "Supplier"), admin);
    assertThat(issued.statusCode()).withFailMessage(issued.body()).isEqualTo(201);
    assertThat(issued.headers().firstValue("Cache-Control")).contains("no-store");
    var body = json.readTree(issued.body());
    String key = body.get("apiKey").asText();
    String keyId = body.get("id").asText();
    assertThat(post("/api/deliveries", upload("sample", valid()), key).statusCode()).isEqualTo(202);
    assertThat(request("/api/keys/" + keyId, "DELETE", supplier).statusCode()).isEqualTo(403);
    assertThat(request("/api/keys/" + key, "DELETE", admin).statusCode()).isEqualTo(404);
    assertThat(request("/api/keys/" + keyId, "DELETE", admin).statusCode()).isEqualTo(204);
    assertThat(post("/api/deliveries", upload("sample", valid()), key).statusCode()).isEqualTo(401);
    assertThat(request("/api/files", "GET", supplier).statusCode()).isEqualTo(403);
    assertThat(request("/api/files", "GET", admin).statusCode()).isEqualTo(200);
    assertThat(request("/ui/files?token=" + admin, "GET", null).statusCode()).isEqualTo(401);
    assertThat(browse(supplier).statusCode()).isEqualTo(403);
    assertThat(browse(admin).statusCode()).isEqualTo(200);
    assertThat(request("/new-unconfigured-endpoint", "GET", admin).statusCode()).isIn(401, 403);
  }

  @Test
  void validatesAndPublishesOnlyAuthorizedModelsWithPublicHistory() throws Exception {
    String valid = submit(valid());
    deliveries.process(valid);
    for (String fixture : List.of("invalid.xtf", "other-model.xtf")) {
      String id = submit(Files.readString(Path.of("dev", fixture)));
      deliveries.process(id);
      assertThat(status(id)).isEqualTo("INVALID");
      assertThat(request("/api/logs/" + id, "GET", null).statusCode()).isEqualTo(200);
    }
    String withDoctype = valid().replace("<TRANSFER ", "<!DOCTYPE TRANSFER><TRANSFER ");
    assertThat(withDoctype).isNotEqualTo(valid());
    String dtd = submit(withDoctype);
    deliveries.process(dtd);
    assertThat(status(dtd)).isEqualTo("INVALID");
    assertThat(status(valid)).isEqualTo("DELIVERED");
    assertThat(Files.readString(TARGET)).isEqualTo(valid());
    assertThat(request("/jobs", "GET", null).body()).contains("Übernahme", "DELIVERED");
    assertThat(request("/api/jobs", "GET", null).statusCode()).isEqualTo(200);
    var filtered = request("/jobs/table?organisation=Supplier&theme=De&operat=samp"
        + "&status=SUCCEEDED&validationStatus=DONE&sort=operat", "GET", null);
    assertThat(filtered.body()).contains(valid, "DELIVERED").doesNotContain(dtd, "INVALID");
    assertThat(request("/web/jobs.xhtml", "GET", null).headers().firstValue("Location"))
        .hasValueSatisfying(location -> assertThat(location).endsWith("/jobs"));
    assertThat(request("/files/Demo/sample.xtf.log", "GET", null).body())
        .isEqualTo(Files.readString(TARGET.resolveSibling("sample.xtf.log")));
    assertThat(request("/ilimodels.xml", "GET", null).statusCode()).isEqualTo(200);
    assertThat(request("/ilisite.xml", "GET", null).statusCode()).isEqualTo(200);
    assertThat(request("/ilidata.xml", "GET", null).body())
        .contains("DatahubDemo", "sample.Demo", "version=2.3");
    jdbc.update("DELETE FROM " + JOBS + " WHERE id=?", valid);
    assertThat(request("/api/jobs/" + valid, "GET", null).body()).contains("DELIVERED");
    assertThat(request("/api/logs/" + valid, "GET", null).statusCode()).isEqualTo(200);

    jdbc.update("UPDATE " + DELIVERIES + " SET finished_at=now() - interval '4 days' WHERE jobid=?", valid);
    assertThat(request("/api/logs/" + valid, "GET", null).statusCode()).isEqualTo(410);
    assertThat(request("/api/jobs/" + valid, "GET", null).body()).contains("Log retention expired");
    new CleanerService(jdbc, files, properties, clock).cleanUp();
    assertThat(Files.exists(files.input(UUID.fromString(valid)))).isFalse();
    assertThat(Files.readString(TARGET)).isEqualTo(valid());
  }

  @Test
  void publishedIndexPathsMatchThePublicDownloadRoutes() throws Exception {
    for (String unavailable : List.of("Areal;1", "Areal%1")) {
      jdbc.update("INSERT INTO test_config.core_operat(aname,theme_r,organisation_r) VALUES (?,1002,1000)",
          unavailable);
      assertThat(post("/api/deliveries", upload(unavailable, valid()), supplier).statusCode())
          .isEqualTo(503);
    }
    jdbc.update("INSERT INTO test_config.core_operat(aname,theme_r,organisation_r) VALUES (?,1002,1000)",
        "Areal #1");
    var response = post("/api/deliveries", upload("Areal #1", valid()), supplier);
    String location = response.headers().firstValue("Operation-Location").orElseThrow();
    try {
      deliveries.process(location.substring(location.lastIndexOf('/') + 1));
      assertThat(request("/ilidata.xml", "GET", null).body()).contains("files/Demo/Areal%20%231.xtf");
      assertThat(request("/files/Demo/Areal%20%231.xtf", "GET", null).body()).isEqualTo(valid());
    } finally {
      Files.deleteIfExists(TARGET.resolveSibling("Areal #1.xtf"));
      Files.deleteIfExists(TARGET.resolveSibling("Areal #1.xtf.log"));
    }
  }

  @Test
  void datasetIndexKeepsLastPublishedVersionDuringPendingAndFailedReplacement() throws Exception {
    String previous = submit(valid());
    deliveries.process(previous);
    assertThat(request("/ilidata.xml", "GET", null).body()).contains(previous);

    String replacement = submit(valid());
    assertThat(request("/ilidata.xml", "GET", null).body())
        .contains(previous).doesNotContain(replacement);

    jdbc.update("UPDATE " + DELIVERIES
        + " SET processing_status='PUBLISHING', isvalid=true,"
        + " result_models='DatahubDemo', transfer_version='2.3' WHERE jobid=?",
        replacement);
    assertThat(request("/ilidata.xml", "GET", null).body())
        .contains(previous).doesNotContain(replacement);

    jdbc.update("UPDATE " + DELIVERIES + " SET processing_status='PUBLICATION_FAILED'"
        + " WHERE jobid=?", replacement);
    assertThat(request("/ilidata.xml", "GET", null).body())
        .contains(previous).doesNotContain(replacement);

    jdbc.update("UPDATE " + DELIVERIES
        + " SET processing_status='PENDING', isvalid=NULL, result_models=NULL"
        + " WHERE jobid=?", replacement);
    deliveries.process(replacement);
    assertThat(request("/ilidata.xml", "GET", null).body())
        .contains(replacement).doesNotContain(previous);

    // Older database versions could publish an XTF without retaining its model metadata.
    jdbc.update("UPDATE " + DELIVERIES + " SET result_models=NULL WHERE jobid=?", replacement);
    assertThat(request("/ilidata.xml", "GET", null).body()).doesNotContain(previous, replacement);
  }

  @Test
  void cleanupDeletesExpiredTerminalFailuresAndOrphansButNotQueuedRetries() throws Exception {
    String failed = submit(valid());
    var input = files.input(UUID.fromString(failed));
    jdbc.update("UPDATE " + DELIVERIES
        + " SET processing_status='PUBLICATION_FAILED', finished_at=now() - interval '4 days'"
        + " WHERE jobid=?", failed);
    jdbc.update("UPDATE " + JOBS + " SET state='SCHEDULED' WHERE id=?", failed);
    var cleaner = new CleanerService(jdbc, files, properties, clock);
    cleaner.cleanUp();
    assertThat(input).exists();

    jdbc.update("UPDATE " + JOBS + " SET state='FAILED' WHERE id=?", failed);
    transactions.executeWithoutResult(tx -> {
      jdbc.queryForList("SELECT jobid FROM " + DELIVERIES + " WHERE jobid=? FOR UPDATE", failed);
      try (var pool = Executors.newSingleThreadExecutor()) {
        pool.submit(cleaner::cleanUp).get(10, TimeUnit.SECONDS);
      } catch (Exception e) {
        throw new AssertionError(e);
      }
      assertThat(input).exists();
    });
    cleaner.cleanUp();
    assertThat(input).doesNotExist();
    assertThat(jdbc.queryForObject("SELECT artifacts_expired FROM " + DELIVERIES
        + " WHERE jobid=?", Boolean.class, failed)).isTrue();

    String interrupted = submit(valid());
    jdbc.update("UPDATE " + JOBS + " SET state='FAILED',"
        + " updatedat=(now() AT TIME ZONE 'UTC') - interval '4 days' WHERE id=?", interrupted);
    cleaner.cleanUp();
    assertThat(status(interrupted)).isEqualTo("FAILED");
    assertThat(files.input(UUID.fromString(interrupted))).doesNotExist();

    UUID orphan = UUID.randomUUID();
    Path orphanDirectory = files.input(orphan).getParent();
    Files.createDirectories(orphanDirectory);
    Files.writeString(orphanDirectory.resolve("input.xtf"), "orphan");
    java.nio.file.Files.setLastModifiedTime(orphanDirectory,
        java.nio.file.attribute.FileTime.from(
            java.time.Instant.now(clock).minus(properties.getArtifactRetention()).minusSeconds(3600)));
    java.nio.file.Files.setLastModifiedTime(orphanDirectory.resolve("input.xtf"),
        java.nio.file.attribute.FileTime.from(
            java.time.Instant.now(clock).minus(properties.getArtifactRetention()).minusSeconds(3600)));

    UUID recent = UUID.randomUUID();
    Path recentDirectory = files.input(recent).getParent();
    Files.createDirectories(recentDirectory);
    Files.writeString(recentDirectory.resolve("input.xtf"), "recent");

    cleaner.cleanUp();
    assertThat(orphanDirectory).doesNotExist();
    assertThat(recentDirectory).exists();
  }

  @Test
  void repairsTheEnqueueGapAndDistinguishesTechnicalFailure() throws Exception {
    String id = submit(valid());
    jdbc.update("DELETE FROM " + JOBS + " WHERE id=?", id);
    assertThat(Files.readString(files.input(UUID.fromString(id)))).isEqualTo(valid());
    deliveries.recoverUnqueuedDeliveries();
    deliveries.recoverUnqueuedDeliveries();
    assertThat(jdbc.queryForObject("SELECT count(*) FROM " + JOBS + " WHERE id=?", Integer.class, id))
        .isEqualTo(1);
    files.delete(UUID.fromString(id));
    assertThatThrownBy(() -> deliveries.process(id)).isInstanceOf(IOException.class);
    assertThat(status(id)).isEqualTo("FAILED");
    assertThat(Files.exists(TARGET)).isFalse();
    files.saveInput(UUID.fromString(id), new ByteArrayInputStream(valid().getBytes(StandardCharsets.UTF_8)));
    deliveries.process(id);
    assertThat(status(id)).isEqualTo("DELIVERED");
    assertThat(Files.readString(TARGET)).isEqualTo(valid());
  }

  @Test
  void retryRepairsAnInterruptedPublicationAndOlderJobsCannotOverwriteIt() throws Exception {
    String previous = submit(valid());
    deliveries.process(previous);
    Path targetLog = TARGET.resolveSibling("sample.xtf.log");
    String previousLog = Files.readString(targetLog);
    String delayed = submit(valid());
    String older = submit(valid());
    String newerContent = valid().replace("sample", "new version") + "\n";
    String newer = submit(newerContent);
    Files.delete(TARGET);
    Files.createDirectory(TARGET);
    assertThatThrownBy(() -> deliveries.process(newer)).isInstanceOf(IOException.class);
    assertThat(status(newer)).isEqualTo("PUBLICATION_FAILED");
    assertThat(Files.readString(targetLog)).isEqualTo(previousLog);

    Files.delete(TARGET);
    deliveries.process(older);
    assertThat(status(older)).isEqualTo("DELIVERED");
    Files.writeString(targetLog, "Interrupted after replacing the log");
    // Model the recovery point before a publication failure could commit, with its JobRunr row missing.
    jdbc.update("UPDATE " + DELIVERIES + " SET processing_status='PUBLISHING' WHERE jobid=?", newer);
    jdbc.update("DELETE FROM " + JOBS + " WHERE id=?", newer);
    deliveries.recoverUnqueuedDeliveries();
    assertThat(jdbc.queryForObject("SELECT count(*) FROM " + JOBS + " WHERE id=?", Integer.class, newer))
        .isEqualTo(1);

    try (var pool = Executors.newFixedThreadPool(2)) {
      var first = pool.submit(() -> { deliveries.process(newer); return null; });
      var duplicate = pool.submit(() -> { deliveries.process(newer); return null; });
      first.get(15, TimeUnit.SECONDS);
      duplicate.get(15, TimeUnit.SECONDS);
    }
    assertThat(status(newer)).isEqualTo("DELIVERED");
    assertThat(Files.readString(TARGET)).isEqualTo(newerContent);
    assertThat(Files.readString(targetLog)).isEqualTo(Files.readString(files.log(UUID.fromString(newer))));
    deliveries.process(delayed);
    assertThat(status(delayed)).isEqualTo("SUPERSEDED");
    assertThat(Files.readString(TARGET)).isEqualTo(newerContent);
  }

  private String createKey(long organisationId) {
    var id = UUID.randomUUID();
    var secret = UUID.randomUUID();
    jdbc.update("INSERT INTO test_config.core_apikey(apikey,createdat,organisation_r)"
        + " VALUES(?,CURRENT_TIMESTAMP,?)",
        ApiKeyFormat.buildStoredValue(id, encoder.encode(secret.toString())), organisationId);
    return ApiKeyFormat.buildApiKey(id, secret);
  }

  private String status(String id) {
    return jdbc.queryForObject("SELECT processing_status FROM " + DELIVERIES + " WHERE jobid=?",
        String.class, id);
  }

  private String submit(String content) throws Exception {
    var response = post("/api/deliveries", upload("sample", content), supplier);
    assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(202);
    String location = response.headers().firstValue("Operation-Location").orElseThrow();
    return location.substring(location.lastIndexOf('/') + 1);
  }

  private String valid() throws IOException {
    return Files.readString(Path.of("dev/valid.xtf"));
  }

  private HttpResponse<String> browse(String key) throws Exception {
    String credentials = Base64.getEncoder().encodeToString(
        key.replace('.', ':').getBytes(StandardCharsets.UTF_8));
    return send(HttpRequest.newBuilder(uri("/ui/files")).header("Authorization", "Basic " + credentials));
  }

  private HttpResponse<String> request(String path, String method, String key) throws Exception {
    var request = HttpRequest.newBuilder(uri(path)).method(method, HttpRequest.BodyPublishers.noBody());
    return send(authenticate(request, key));
  }

  private HttpResponse<String> post(String path, String body, String key) throws Exception {
    var request = HttpRequest.newBuilder(uri(path))
        .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
        .POST(HttpRequest.BodyPublishers.ofString(body + "--" + BOUNDARY + "--\r\n"));
    return send(authenticate(request, key));
  }

  private HttpRequest.Builder authenticate(HttpRequest.Builder request, String key) {
    return key == null ? request : request.header("X-API-KEY", key);
  }

  private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
    return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private URI uri(String path) {
    return URI.create("http://localhost:" + port + path);
  }

  private static String upload(String operat, String content) {
    return part("theme", "Demo") + part("operat", operat)
        + "--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"input.xtf\"\r\n"
        + "Content-Type: application/octet-stream\r\n\r\n" + content + "\r\n";
  }

  private static String part(String name, String value) {
    return "--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"" + name
        + "\"\r\n\r\n" + value + "\r\n";
  }
}
