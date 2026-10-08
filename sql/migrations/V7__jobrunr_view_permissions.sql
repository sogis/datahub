-- Recreating a view drops its grants. Application startup must work without DDL rights.
GRANT SELECT ON ${jobSchema}.jobrunr_jobs_stats TO datahub, datahub_read;
GRANT SELECT ON ALL TABLES IN SCHEMA ${logSchema} TO datahub_read;
