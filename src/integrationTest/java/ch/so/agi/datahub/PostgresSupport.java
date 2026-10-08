package ch.so.agi.datahub;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.flywaydb.core.Flyway;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.*;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class PostgresSupport {
  protected static final Network NETWORK = Network.newNetwork();
  protected static final PostgreSQLContainer DATABASE =
      new PostgreSQLContainer("postgres:16-alpine")
          .withNetwork(NETWORK)
          .withNetworkAliases("database");
  protected static final GenericContainer<?> MAIL =
      new GenericContainer<>("axllent/mailpit:v1.31.4")
          .withExposedPorts(1025, 8025)
          .withNetwork(NETWORK)
          .withNetworkAliases("mail");
  protected static final Path ROOT;

  static {
    try {
      ROOT = Files.createTempDirectory("datahub-postgres-tests-");
    } catch (java.io.IOException e) {
      throw new ExceptionInInitializerError(e);
    }
    DATABASE.start();
    MAIL.start();
    Flyway.configure()
        .dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
        .locations("filesystem:sql/migrations")
        .placeholders(
            Map.of(
                "configSchema", "test_config", "logSchema", "test_log", "jobSchema", "test_jobs"))
        .load()
        .migrate();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
    registry.add("spring.datasource.username", DATABASE::getUsername);
    registry.add("spring.datasource.password", DATABASE::getPassword);
    registry.add("app.dbSchemaConfig", () -> "test_config");
    registry.add("app.dbSchemaLog", () -> "test_log");
    registry.add("app.dbSchemaJob", () -> "test_jobs");
    registry.add("app.workDirectory", () -> ROOT.resolve("work").toString());
    registry.add("app.targetDirectory", () -> ROOT.resolve("target").toString());
    registry.add("app.publicBaseUrl", () -> "http://example.test/datahub");
    registry.add("app.preferredIliRepo", () -> Path.of("dev/models").toAbsolutePath().toString());
    registry.add("app.defaultIliRepositories", () -> false);
    registry.add("app.adminAccountInit", () -> false);
    registry.add("app.cleanerEnabled", () -> false);
    registry.add("app.mailEnabled", () -> true);
    registry.add("app.mailFrom", () -> "sender@example.test");
    registry.add("jobrunr.database.table-prefix", () -> "test_jobs.");
    registry.add("jobrunr.database.skip-create", () -> true);
    registry.add("spring.mail.host", MAIL::getHost);
    registry.add("spring.mail.port", () -> MAIL.getMappedPort(1025));
    registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
    registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> false);
    registry.add("gg.jte.development-mode", () -> true);
    registry.add("spring.devtools.restart.enabled", () -> false);
  }
}
