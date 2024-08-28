///usr/bin/env jbang "$0" "$@" ; exit $?
//REPOS mavenCentral,ehi=https://jars.interlis.ch/
//DEPS ch.interlis:ili2pg:4.9.1 org.postgresql:postgresql:42.1.4 org.jobrunr:jobrunr:6.3.4 org.slf4j:slf4j-api:2.0.12

import static java.lang.System.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

import org.jobrunr.storage.sql.SqlStorageProvider;
import org.jobrunr.storage.sql.common.DatabaseCreator;
import org.jobrunr.storage.sql.common.migrations.DefaultSqlMigrationProvider;
import org.jobrunr.storage.sql.common.migrations.RunningOnJava11OrLowerWithinFatJarSqlMigrationProvider;
import org.jobrunr.storage.sql.common.migrations.SqlMigration;
import org.jobrunr.storage.sql.common.migrations.SqlMigrationProvider;
import org.jobrunr.storage.sql.common.tables.AnsiDatabaseTablePrefixStatementUpdater;
import org.jobrunr.storage.sql.common.tables.TablePrefixStatementUpdater;
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider;
import org.jobrunr.utils.RuntimeUtils;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;

public class create_schema_sql {

    private static final String DB_SCHEMA_CONFIG = "agi_datahub_config_v1";
    private static final String DB_SCHEMA_LOG = "agi_datahub_log_v1";
    private static final String DB_SCHEMA_JOBRUNR = "agi_datahub_jobrunr_v1";

    private static final String DB_APP_USER = "datahub";

    private static final Map<String,String> iliSchemas = Map.of(
            DB_SCHEMA_CONFIG, "SO_AGI_Datahub_Config_20240403", 
            DB_SCHEMA_LOG, "SO_AGI_Datahub_Log_20240403"
        );

    private static final Map<String, Class<? extends SqlStorageProvider>> databaseTypes = Map.of(
    "postgres", PostgresStorageProvider.class);
    
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    public static void main(String... args) throws Ili2dbException, IOException {
        out.println("Hello World");

        StringBuilder contentBuilder = new StringBuilder();

        // Config- und Log-Schema mit ili2pg erzeugen
        Config config = new Config();
        new PgMain().initConfig(config);
        config.setFunction(Config.FC_SCRIPT);
        config.setModeldir("https://geo.so.ch/models;http://models.geo.admin.ch");
        Config.setStrokeArcs(config, Config.STROKE_ARCS_ENABLE);
        // https://github.com/claeis/ili2db/issues/548
        //config.setCreateFk(Config.CREATE_FK_YES);
        config.setCreateFkIdx(Config.CREATE_FKIDX_YES);
        config.setCreateImportTabs(true);
        config.setCreateMetaInfo(true);
        config.setNameOptimization(Config.NAME_OPTIMIZATION_TOPIC);
        config.setDefaultSrsCode("2056");
        config.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        //config.setTidHandling(Config.TID_HANDLING_PROPERTY);
        //config.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
        //config.setCreateTypeDiscriminator(Config.CREATE_TYPE_DISCRIMINATOR_ALWAYS);

        for (Map.Entry<String, String> entry : iliSchemas.entrySet()) {
            String schema = entry.getKey();
            String model = entry.getValue();
            String fileName = Paths.get(tempDir, schema+".sql").toString();
            System.err.println(fileName);

            config.setDbschema(schema);
            config.setModels(model);
            config.setCreatescript(new File(fileName).getAbsolutePath());
            Ili2db.run(config, null);

            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            contentBuilder.append(content);        
        }

        // Jobrunr-Schema und -Tabellen erzeugen
        contentBuilder.append("\n");
        contentBuilder.append("CREATE SCHEMA IF NOT EXISTS " + DB_SCHEMA_JOBRUNR + ";");

        Class<? extends SqlStorageProvider> sqlStorageProviderClass = databaseTypes.get("postgres");
        DatabaseMigrationsProvider databaseMigrationsProvider = new DatabaseMigrationsProvider(sqlStorageProviderClass);
        TablePrefixStatementUpdater statementUpdater = getStatementUpdater(DB_SCHEMA_JOBRUNR+".", sqlStorageProviderClass);
        databaseMigrationsProvider.getMigrations()
                .forEach(sqlMigration -> createSQLMigrationFile(sqlMigration, statementUpdater));
        System.err.println("Successfully created all SQL scripts for postgres!");


        List<Path> migrationFiles = new ArrayList<Path>();
        try (Stream<Path> walk = Files.walk(Paths.get("."))) {
            migrationFiles = walk
                    .filter(p -> !Files.isDirectory(p))
                    .filter(f -> {
                        if (f.getFileName().toString().startsWith("v0") && f.getFileName().toString().endsWith(".sql")) {
                            return true;
                        }
                        return false; 
                    })
                    .collect(Collectors.toList());        
        }
        Collections.sort(migrationFiles);

        for (Path migrationFile : migrationFiles) {
            String content = Files.readString(migrationFile);
            contentBuilder.append("\n").append(content);
            Files.delete(migrationFile);
        }

        contentBuilder.append("\n");
        contentBuilder.append("COMMENT ON SCHEMA "+DB_SCHEMA_JOBRUNR+" IS 'Schema für Jobrunr';");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT USAGE ON SCHEMA "+DB_SCHEMA_JOBRUNR+" TO datahub;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA "+DB_SCHEMA_JOBRUNR+" TO datahub;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT USAGE ON ALL SEQUENCES IN SCHEMA "+DB_SCHEMA_JOBRUNR+" TO datahub;");

        // View-Definition aus Datei lesen und hinzufügen
        String viewSql = Files.readString(Paths.get("view.sql"))
            .replace("${DB_SCHEMA_CONFIG}", DB_SCHEMA_CONFIG)
            .replace("${DB_SCHEMA_LOG}", DB_SCHEMA_LOG)
            .replace("${DB_SCHEMA_JOBRUNR}", DB_SCHEMA_JOBRUNR);
        contentBuilder.append("\n").append(viewSql);

        // Rechte für datahub user setzen. Kann erst nach View-Definition gemacht werden.
        for (Map.Entry<String, String> entry : iliSchemas.entrySet()) {
            String schema = entry.getKey();

            contentBuilder.append("\n");
            contentBuilder.append("COMMENT ON SCHEMA "+schema+" IS 'Schema für Datahub';");
            contentBuilder.append("\n");
            contentBuilder.append("GRANT USAGE ON SCHEMA "+schema+" TO datahub;");
            contentBuilder.append("\n");
            contentBuilder.append("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA "+schema+" TO datahub;");
            contentBuilder.append("\n");
            contentBuilder.append("GRANT USAGE ON ALL SEQUENCES IN SCHEMA "+schema+" TO datahub;");
            contentBuilder.append("\n");
        }

        contentBuilder.append("\n");
        contentBuilder.append("CREATE ROLE datahub_read;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT USAGE ON SCHEMA " + DB_SCHEMA_CONFIG + " TO datahub_read;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT SELECT ON ALL TABLES IN SCHEMA " + DB_SCHEMA_CONFIG + " TO datahub_read;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT USAGE ON SCHEMA " + DB_SCHEMA_LOG + " TO datahub_read;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT SELECT ON ALL TABLES IN SCHEMA " + DB_SCHEMA_LOG + " TO datahub_read;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT USAGE ON SCHEMA " + DB_SCHEMA_JOBRUNR + " TO datahub_read;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT SELECT ON ALL TABLES IN SCHEMA " + DB_SCHEMA_JOBRUNR + " TO datahub_read;");
        contentBuilder.append("\n");
        contentBuilder.append("CREATE ROLE datahub_write;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT USAGE ON SCHEMA " + DB_SCHEMA_CONFIG + " TO datahub_write;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA " + DB_SCHEMA_CONFIG + " TO datahub_write;");
        contentBuilder.append("\n");
        contentBuilder.append("GRANT USAGE ON ALL SEQUENCES IN SCHEMA " + DB_SCHEMA_CONFIG + " TO datahub_write;");
        contentBuilder.append("\n");

        //String outputFile = Paths.get(tempDir, "setup_gdi.sql").toString();
        String outputFile = Paths.get( "setup_gdi.sql").toString();
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(contentBuilder.toString().getBytes());
        fos.close();
        System.err.println(outputFile);
    }

    private static void createSQLMigrationFile(SqlMigration migration, TablePrefixStatementUpdater statementUpdater) {
        try {
            final StringBuilder result = new StringBuilder();
            final String sql = migration.getMigrationSql();
            for (String statement : sql.split(";")) {
                result.append(statementUpdater.updateStatement(statement)).append(";");
            }
            Files.write(Paths.get("./" + migration.getFileName()), result.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static TablePrefixStatementUpdater getStatementUpdater(String tablePrefix, Class<? extends SqlStorageProvider> sqlStorageProviderClass) {
        return new AnsiDatabaseTablePrefixStatementUpdater(tablePrefix);
    }
}

class DatabaseMigrationsProvider {

    private final Class<? extends SqlStorageProvider> sqlStorageProviderClass;

    public DatabaseMigrationsProvider(Class<? extends SqlStorageProvider> sqlStorageProviderClass) {
        this.sqlStorageProviderClass = sqlStorageProviderClass;
    }

    public Stream<SqlMigration> getMigrations() {
        SqlMigrationProvider migrationProvider = getMigrationProvider();

        try {
            final Map<String, SqlMigration> commonMigrations = getCommonMigrations(migrationProvider).stream().collect(toMap(SqlMigration::getFileName, m -> m));
            final Map<String, SqlMigration> databaseSpecificMigrations = getDatabaseSpecificMigrations(migrationProvider).stream().collect(toMap(SqlMigration::getFileName, p -> p));

            final HashMap<String, SqlMigration> actualMigrations = new HashMap<>(commonMigrations);
            actualMigrations.putAll(databaseSpecificMigrations);

            return actualMigrations.values().stream();
        } catch (IllegalStateException e) {
            if(e.getMessage().startsWith("Duplicate key")) {
                throw new IllegalStateException("It seems you have JobRunr twice on your classpath. Please make sure to only have one JobRunr jar in your classpath.", e);
            }
            throw e;
        }
    }

    private SqlMigrationProvider getMigrationProvider() {
        if (RuntimeUtils.getJvmVersion() < 12 && RuntimeUtils.isRunningFromNestedJar()) {
            return new RunningOnJava11OrLowerWithinFatJarSqlMigrationProvider();
        } else {
            return new DefaultSqlMigrationProvider();
        }
    }

    protected List<SqlMigration> getCommonMigrations(SqlMigrationProvider migrationProvider) {
        return migrationProvider.getMigrations(DatabaseCreator.class);
    }

    protected List<SqlMigration> getDatabaseSpecificMigrations(SqlMigrationProvider migrationProvider) {
        if (sqlStorageProviderClass != null) {
            return migrationProvider.getMigrations(sqlStorageProviderClass);
        }
        return emptyList();
    }
}

