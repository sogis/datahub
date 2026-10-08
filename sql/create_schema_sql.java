/// usr/bin/env jbang "$0" "$@" ; exit $?
// REPOS mavenCentral,ehi=https://jars.interlis.ch/
// DEPS ch.interlis:ili2pg:4.9.1 org.postgresql:postgresql:42.6.0

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;
import java.nio.file.*;
import java.util.Map;

/** Generate reviewable SQL from the authoritative local models. Never applies DDL. */
public class create_schema_sql {
  public static void main(String... args) throws Exception {
    Path output = Path.of("build/model-ddl");
    Files.createDirectories(output);
    for (var entry :
        Map.of(
                "agi_datahub_config_v1",
                "SO_AGI_Datahub_Config_20240403",
                "agi_datahub_log_v1",
                "SO_AGI_Datahub_Log_20240403")
            .entrySet()) {
      var config = new Config();
      new PgMain().initConfig(config);
      config.setFunction(Config.FC_SCRIPT);
      config.setModeldir(Path.of("dev").toAbsolutePath().toString());
      config.setCreateFk(Config.CREATE_FK_YES);
      config.setCreateFkIdx(Config.CREATE_FKIDX_YES);
      config.setCreateUniqueConstraints(true);
      config.setCreateImportTabs(true);
      config.setCreateMetaInfo(true);
      config.setNameOptimization(Config.NAME_OPTIMIZATION_TOPIC);
      config.setDbschema(entry.getKey());
      config.setModels(entry.getValue());
      config.setCreatescript(output.resolve(entry.getKey() + ".sql").toAbsolutePath().toString());
      Ili2db.run(config, null);
    }
    System.out.println(
        "SQL drafts in build/model-ddl; compare names and constraints with versioned migrations.");
  }
}
