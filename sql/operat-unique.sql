-- ili2pg 5.5.2 does not emit the INTERLIS UNIQUE name, Theme_R index.
CREATE UNIQUE INDEX IF NOT EXISTS core_operat_theme_name_key
    ON agi_datahub_config_v1.core_operat(theme_r, aname);
