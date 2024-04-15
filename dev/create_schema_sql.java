///usr/bin/env jbang "$0" "$@" ; exit $?
//REPOS mavenCentral,ehi=https://jars.interlis.ch/
//DEPS ch.interlis:ili2pg:4.9.1 org.postgresql:postgresql:42.1.4 

import static java.lang.System.*;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;

public class create_schema_sql {

    private static final String DB_SCHEMA_CONFIG = "agi_datahub_config_v1";
    private static final String DB_SCHEMA_LOG = "agi_datahub_log_v1";
    private static final String DB_SCHEMA_JOBRUNR = "agi_datahub_jobrunr_v1";

    public static void main(String... args) {
        out.println("Hello World");

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

    }
}
