package ch.so.agi.datahub.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.interlis2.validator.Validator;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2h2gis.H2gisMain;
import ch.so.agi.datahub.service.DeliveryValidationService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(DeliveryE2ETest.E2eTestConfig.class)
class DeliveryE2ETest {
    private static final AtomicBoolean DB_INITIALIZED = new AtomicBoolean(false);
    private static final Path BASE_DIR = createTempDir();
    private static final Path DB_FILE = BASE_DIR.resolve("datahub-e2e-db");
    private static final Path WORK_DIR = BASE_DIR.resolve("work");
    private static final Path TARGET_DIR = BASE_DIR.resolve("target");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StorageProvider storageProvider;

    @TestConfiguration
    static class E2eTestConfig {
        @Bean
        @Primary
        DeliveryValidationService deliveryValidationService() {
            return (filePath, settings) -> {
                String logFile = settings.getValue(Validator.SETTING_LOGFILE);
                if (logFile != null) {
                    try {
                        Files.writeString(Paths.get(logFile), "E2E validation skipped.");
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to write validation log", e);
                    }
                }
                return true;
            };
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        initializeDatabase();
        registry.add("spring.datasource.url", () -> h2JdbcUrl());
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("app.workDirectory", () -> WORK_DIR.toString());
        registry.add("app.targetDirectory", () -> TARGET_DIR.toString());
        registry.add("app.folderPrefix", () -> "e2e_");
        registry.add("org.jobrunr.database.tablePrefix", () -> "agi_datahub_jobrunr_v1.");
        registry.add("org.jobrunr.database.skip-create", () -> "false");
        registry.add("org.jobrunr.background-job-server.enabled", () -> "true");
        registry.add("org.jobrunr.dashboard.enabled", () -> "false");
        registry.add("app.mailEnabled", () -> "false");
        registry.add("app.preferredIliRepo", () -> "https://geo.so.ch/models");
    }

    @Test
    void deliveryRunsThroughEndToEndPipeline() {
        Path xtfFile = Paths.get("src/test/data/ch.so.avt.kunstbauten.xtf").toAbsolutePath();
        assertThat(xtfFile).exists();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(xtfFile));
        body.add("theme", "SO_AVT_Kunstbauten");
        body.add("operat", "kanton");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-API-KEY", "dbe60a63-2bdd-492a-a81d-338e7fdc6abb.1c7bef05-c947-4080-9e17-26207bff55d8");

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/deliveries",
                new HttpEntity<>(body, headers),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        String operationLocation = response.getHeaders().getFirst("Operation-Location");
        assertThat(operationLocation).isNotBlank();
        String jobId = operationLocation.substring(operationLocation.lastIndexOf('/') + 1);

        AtomicReference<StateName> jobStateRef = new AtomicReference<>();
        await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(2))
                .until(() -> {
                    try {
                        Job job = storageProvider.getJobById(UUID.fromString(jobId));
                        StateName stateName = job.getState();
                        jobStateRef.set(stateName);
                        return stateName == StateName.SUCCEEDED || stateName == StateName.FAILED;
                    } catch (JobNotFoundException ex) {
                        return false;
                    }
                });

        assertThat(jobStateRef.get()).isEqualTo(StateName.SUCCEEDED);

        Path deliveredFile = TARGET_DIR.resolve("SO_AVT_Kunstbauten").resolve("kanton.xtf");
        Path deliveredLogFile = TARGET_DIR.resolve("SO_AVT_Kunstbauten").resolve("kanton.xtf.log");
        assertThat(deliveredFile).exists();
        assertThat(deliveredLogFile).exists();

        ResponseEntity<String> logResponse = restTemplate.getForEntity("/api/logs/" + jobId, String.class);
        assertThat(logResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logResponse.getBody()).isNotBlank();
    }

    private static void initializeDatabase() {
        if (!DB_INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        try {
            Files.createDirectories(WORK_DIR);
            Files.createDirectories(TARGET_DIR);
            try (Connection connection = DriverManager.getConnection(h2JdbcUrl(), "sa", "")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE SCHEMA IF NOT EXISTS agi_datahub_jobrunr_v1");
                }
            }

            runSchemaImport("agi_datahub_config_v1", "SO_AGI_Datahub_Config_20240403");
            runSchemaImport("agi_datahub_log_v1", "SO_AGI_Datahub_Log_20240403");

            Path xtfFile = Paths.get("dev/datahub_20260122.xtf").toAbsolutePath();
            runDataImport("agi_datahub_config_v1", "SO_AGI_Datahub_Config_20240403", xtfFile);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize E2E database", e);
        }
    }


    private static void runSchemaImport(String schema, String model) throws Ili2dbException {
        Config config = createBaseConfig(schema, model);
        config.setFunction(Config.FC_SCHEMAIMPORT);
        Ili2db.run(config, null);
    }

    private static void runDataImport(String schema, String model, Path xtfFile) throws Ili2dbException {
        Config config = createBaseConfig(schema, model);
        config.setFunction(Config.FC_IMPORT);
        config.setXtffile(xtfFile.toString());
        Ili2db.readSettingsFromDb(config);
        Ili2db.run(config, null);
    }

    private static Config createBaseConfig(String schema, String model) throws Ili2dbException {
        Config config = new Config();
        new H2gisMain().initConfig(config);
        config.setDbfile(DB_FILE.toString());
        config.setDburl(h2JdbcUrl());
        config.setDbschema(schema);
        config.setDbusr("sa");
        config.setDbpwd("");
        config.setDefaultSrsCode("2056");
        config.setCreateFk(Config.CREATE_FK_YES);
        config.setCreateFkIdx(Config.CREATE_FKIDX_YES);
        config.setCreateEnumDefs(Config.CREATE_ENUM_DEFS_MULTI);
        config.setCreateMetaInfo(true);
        config.setNameOptimization(Config.NAME_OPTIMIZATION_TOPIC);
        config.setStrokeArcs(Config.STROKE_ARCS_ENABLE);
        config.setCreateUniqueConstraints(true);
        config.setCreateNumChecks(true);
        config.setCreateTextChecks(true);
        config.setCreateDateTimeChecks(true);
        config.setCreateImportTabs(true);
        config.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);

        Path repoRoot = Paths.get("").toAbsolutePath();
        Path devDir = repoRoot.resolve("dev");
        String modeldir = devDir + ";https://geo.so.ch/models;http://models.geo.admin.ch";
        config.setModeldir(modeldir);
        config.setModels(model);
        return config;
    }

    private static String h2JdbcUrl() {
        return "jdbc:h2:file:" + DB_FILE + ";MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE";
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("datahub-e2e-");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create temp directory", e);
        }
    }
}
