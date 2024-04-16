///usr/bin/env jbang "$0" "$@" ; exit $?
//REPOS mavenCentral,ehi=https://jars.interlis.ch/
//DEPS ch.interlis:ili2pg:4.9.1 org.postgresql:postgresql:42.1.4 

import static java.lang.System.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;

public class create_schema_sql {

    // private static final String DB_SCHEMA_CONFIG = "agi_datahub_config_v1";
    // private static final String DB_SCHEMA_LOG = "agi_datahub_log_v1";
    private static final String DB_SCHEMA_JOBRUNR = "agi_datahub_jobrunr_v1";

    private static final Map<String,String> iliSchemas = Map.of(
            "agi_datahub_config_v1", "SO_AGI_Datahub_Config_20240403", 
            "agi_datahub_log_v1", "SO_AGI_Datahub_Log_20240403"
        );

    private static final String tempDir = System.getProperty("java.io.tmpdir");

    public static void main(String... args) throws Ili2dbException, IOException {
        out.println("Hello World");

        StringBuilder contentBuilder = new StringBuilder();

        Config config = new Config();
        new PgMain().initConfig(config);
        config.setFunction(Config.FC_SCRIPT);
        config.setModeldir("https://geo.so.ch/models;http://models.geo.admin.ch");
        Config.setStrokeArcs(config, Config.STROKE_ARCS_ENABLE);
        config.setCreateFk(Config.CREATE_FK_YES);
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

        
        String outputFile = Paths.get(tempDir, "setup_tmp.sql").toString();
        FileOutputStream tmpFos = new FileOutputStream(outputFile);
        tmpFos.write(contentBuilder.toString().getBytes());
        tmpFos.close();


        System.err.println(outputFile);


    }
}
