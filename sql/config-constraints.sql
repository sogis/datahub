-- Close constraints missing from various installation paths. Safe to run on an existing R0 schema.
CREATE UNIQUE INDEX IF NOT EXISTS core_organisation_aname_key
    ON agi_datahub_config_v1.core_organisation(aname);
CREATE UNIQUE INDEX IF NOT EXISTS core_theme_aname_key
    ON agi_datahub_config_v1.core_theme(aname);

-- ili2pg 5.5.2 does not emit the INTERLIS UNIQUE name, Theme_R index.
CREATE UNIQUE INDEX IF NOT EXISTS core_operat_theme_name_key
    ON agi_datahub_config_v1.core_operat(theme_r, aname);

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_constraint WHERE conname='core_apikey_organisation_r_fkey'
                 AND conrelid='agi_datahub_config_v1.core_apikey'::regclass) THEN
    ALTER TABLE agi_datahub_config_v1.core_apikey
        ADD CONSTRAINT core_apikey_organisation_r_fkey FOREIGN KEY (organisation_r)
            REFERENCES agi_datahub_config_v1.core_organisation DEFERRABLE INITIALLY DEFERRED;
  END IF;
  IF NOT EXISTS (SELECT FROM pg_constraint WHERE conname='core_operat_organisation_r_fkey'
                 AND conrelid='agi_datahub_config_v1.core_operat'::regclass) THEN
    ALTER TABLE agi_datahub_config_v1.core_operat
        ADD CONSTRAINT core_operat_organisation_r_fkey FOREIGN KEY (organisation_r)
            REFERENCES agi_datahub_config_v1.core_organisation DEFERRABLE INITIALLY DEFERRED;
  END IF;
  IF NOT EXISTS (SELECT FROM pg_constraint WHERE conname='core_operat_theme_r_fkey'
                 AND conrelid='agi_datahub_config_v1.core_operat'::regclass) THEN
    ALTER TABLE agi_datahub_config_v1.core_operat
        ADD CONSTRAINT core_operat_theme_r_fkey FOREIGN KEY (theme_r)
            REFERENCES agi_datahub_config_v1.core_theme DEFERRABLE INITIALLY DEFERRED;
  END IF;
END $$;
