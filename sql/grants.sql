-- Run after schema creation or the offline JobRunr table creator, as schema owner.
GRANT USAGE ON SCHEMA agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname='datahub_read') THEN
    GRANT SELECT ON agi_datahub_jobrunr_v1.jobrunr_jobs_stats TO datahub_read;
  END IF;
END $$;
