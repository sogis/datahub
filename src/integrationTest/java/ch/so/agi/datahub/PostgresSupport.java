package ch.so.agi.datahub;

import java.nio.file.*;
import org.jobrunr.storage.sql.common.DatabaseCreator;
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class PostgresSupport {
  protected static final Network NETWORK = Network.newNetwork();
  protected static final PostgreSQLContainer DATABASE =
      new PostgreSQLContainer(DockerImageName.parse("sogis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")).withNetwork(NETWORK).withNetworkAliases("database");
  protected static final Path ROOT;

  static {
    try {
      ROOT = Files.createTempDirectory("datahub-postgres-tests-");
      DATABASE.start();
      initialize("test");
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  protected static JdbcTemplate initialize(String prefix) throws Exception {
    var source = new DriverManagerDataSource(
        DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
    var jdbc = new JdbcTemplate(source);
    for (String model : new String[]{"config", "log"}) {
      String ddl = Files.readString(Path.of("build/schema/" + model + ".sql"));
      jdbc.execute(ddl.replace("agi_datahub_" + model + "_v1", prefix + "_" + model));
    }
    jdbc.execute("CREATE SCHEMA " + prefix + "_jobs");
    new DatabaseCreator(source, prefix + "_jobs.", PostgresStorageProvider.class).runMigrations();
    return jdbc;
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
    registry.add("spring.datasource.username", DATABASE::getUsername);
    registry.add("spring.datasource.password", DATABASE::getPassword);
    registry.add("app.dbSchemaConfig", () -> "test_config");
    registry.add("app.dbSchemaLog", () -> "test_log");
    registry.add("app.workDirectory", () -> ROOT.resolve("work").toString());
    registry.add("app.targetDirectory", () -> ROOT.resolve("target").toString());
    registry.add("app.publicBaseUrl", () -> "http://example.test/datahub");
    registry.add("app.preferredIliRepo", () -> Path.of("dev/models").toAbsolutePath().toString());
    registry.add("app.defaultIliRepositories", () -> false);
    registry.add("app.cleanerEnabled", () -> false);
    registry.add("jobrunr.database.table-prefix", () -> "test_jobs.");
    registry.add("spring.devtools.restart.enabled", () -> false);
  }
}
