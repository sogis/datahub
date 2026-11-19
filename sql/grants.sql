-- Append after the schema dump when generating setup_gdi.sql.
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'datahub_read') THEN
    CREATE ROLE datahub_read NOLOGIN;
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'datahub_write') THEN
    CREATE ROLE datahub_write NOLOGIN;
  END IF;
END $$;

GRANT USAGE ON SCHEMA agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;

GRANT USAGE ON SCHEMA agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub_read;
GRANT SELECT ON ALL TABLES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub_read;

GRANT USAGE ON SCHEMA agi_datahub_config_v1 TO datahub_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_datahub_config_v1 TO datahub_write;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_datahub_config_v1 TO datahub_write;
