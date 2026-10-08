package ch.so.agi.datahub.migration;

import java.util.Map;
import org.flywaydb.core.Flyway;

/** Explicit tooling entry point; never called by the production application. */
public final class SchemaMigration {
    public static void main(String[] args) {
        Flyway flyway = Flyway.configure()
                .dataSource(env("DBURL", "jdbc:postgresql://localhost:55432/edit"),
                        env("DBUSR", "postgres"), env("DBPWD", "secret"))
                .locations("filesystem:sql/migrations")
                .placeholders(Map.of("configSchema", schema("CONFIG_DB_SCHEMA", "agi_datahub_config_v1"),
                        "logSchema", schema("LOG_DB_SCHEMA", "agi_datahub_log_v1"),
                        "jobSchema", schema("JOBRUNR_DB_SCHEMA", "agi_datahub_jobrunr_v1")))
                .baselineVersion("1").cleanDisabled(true).load();
        if (args.length == 1 && args[0].equals("baseline")) flyway.baseline();
        else if (args.length == 0 || args[0].equals("migrate")) flyway.migrate();
        else throw new IllegalArgumentException("Expected migrate or baseline");
    }

    private static String schema(String name, String fallback) {
        String value = env(name, fallback);
        if (!value.matches("[a-z][a-z0-9_]*")) throw new IllegalArgumentException("Invalid schema: " + name);
        return value;
    }

    private static String env(String name, String fallback) {
        return System.getenv().getOrDefault(name, fallback);
    }
}
