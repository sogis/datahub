-- One-time upgrade as schema owner with the previous application stopped and its jobs drained.
\set ON_ERROR_STOP on
BEGIN;

LOCK TABLE agi_datahub_config_v1.core_apikey, agi_datahub_config_v1.core_organisation,
    agi_datahub_config_v1.core_theme,
    agi_datahub_config_v1.core_operat, agi_datahub_log_v1.deliveries_delivery,
    agi_datahub_jobrunr_v1.jobrunr_jobs IN ACCESS EXCLUSIVE MODE;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema='agi_datahub_log_v1' AND table_name='deliveries_delivery'
               AND column_name='processing_status') THEN
    RAISE EXCEPTION 'The R0 schema upgrade has already been applied.';
  END IF;
  IF EXISTS (SELECT 1 FROM agi_datahub_jobrunr_v1.jobrunr_jobs
             WHERE state NOT IN ('SUCCEEDED', 'FAILED', 'DELETED'))
     OR EXISTS (SELECT 1 FROM agi_datahub_jobrunr_v1.jobrunr_recurring_jobs) THEN
    RAISE EXCEPTION 'Drain active JobRunr jobs and remove recurring schedules before upgrading.';
  END IF;
END $$;

ALTER TABLE agi_datahub_config_v1.core_theme ADD COLUMN IF NOT EXISTS expected_models varchar(2048);
CREATE UNIQUE INDEX IF NOT EXISTS core_organisation_aname_key
    ON agi_datahub_config_v1.core_organisation(aname);
CREATE UNIQUE INDEX IF NOT EXISTS core_theme_aname_key
    ON agi_datahub_config_v1.core_theme(aname);
\ir operat-unique.sql

-- Use the same foreign keys as a fresh ili2pg schema. Older DDL omitted these.
ALTER TABLE agi_datahub_config_v1.core_apikey
    DROP CONSTRAINT IF EXISTS core_apikey_organisation_r_fkey,
    ADD CONSTRAINT core_apikey_organisation_r_fkey FOREIGN KEY (organisation_r)
        REFERENCES agi_datahub_config_v1.core_organisation DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE agi_datahub_config_v1.core_operat
    DROP CONSTRAINT IF EXISTS core_operat_organisation_r_fkey,
    DROP CONSTRAINT IF EXISTS core_operat_theme_r_fkey,
    ADD CONSTRAINT core_operat_organisation_r_fkey FOREIGN KEY (organisation_r)
        REFERENCES agi_datahub_config_v1.core_organisation DEFERRABLE INITIALLY DEFERRED,
    ADD CONSTRAINT core_operat_theme_r_fkey FOREIGN KEY (theme_r)
        REFERENCES agi_datahub_config_v1.core_theme DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE agi_datahub_log_v1.deliveries_delivery
    ADD COLUMN processing_status varchar(20),
    ADD COLUMN result_models text,
    ADD COLUMN transfer_version varchar(10),
    ADD COLUMN validator_version varchar(128),
    ADD COLUMN legacy_log varchar(100),
    ADD COLUMN IF NOT EXISTS email varchar(512),
    ADD COLUMN IF NOT EXISTS validator_config varchar(512),
    ADD COLUMN IF NOT EXISTS validator_meta_config varchar(512),
    ADD COLUMN IF NOT EXISTS expected_models varchar(2048),
    ADD COLUMN IF NOT EXISTS notification_status varchar(20),
    ADD COLUMN IF NOT EXISTS updated_at timestamp,
    ADD COLUMN IF NOT EXISTS finished_at timestamp,
    ADD COLUMN IF NOT EXISTS artifacts_expired boolean;

CREATE UNIQUE INDEX IF NOT EXISTS datahub_delivery_jobid_unique
    ON agi_datahub_log_v1.deliveries_delivery(jobid);

UPDATE agi_datahub_log_v1.deliveries_delivery d
SET updated_at = COALESCE(d.updated_at, (j.updatedat AT TIME ZONE 'UTC') AT TIME ZONE 'Europe/Zurich'),
    finished_at = COALESCE(d.finished_at, (j.updatedat AT TIME ZONE 'UTC') AT TIME ZONE 'Europe/Zurich')
FROM agi_datahub_jobrunr_v1.jobrunr_jobs j
WHERE j.id = d.jobid::char(36);

-- Original Datahub allowed invalid transfers in the target. Preserve that historical fact.
UPDATE agi_datahub_log_v1.deliveries_delivery
SET processing_status = CASE
      WHEN isdelivered THEN 'DELIVERED'
      WHEN isvalid IS FALSE THEN 'INVALID'
      WHEN isvalid THEN 'PUBLICATION_FAILED'
      ELSE 'FAILED'
    END,
    finished_at = COALESCE(finished_at, deliverydate);

-- Databases that already ran the modernization branch have three additional tables.
DO $$
BEGIN
  IF to_regclass('agi_datahub_log_v1.validation_attempt') IS NULL THEN
    RETURN;
  END IF;
  IF EXISTS (SELECT 1 FROM agi_datahub_log_v1.validation_attempt WHERE state IN ('ENQUEUED','PROCESSING'))
     OR EXISTS (SELECT 1 FROM agi_datahub_log_v1.notification WHERE state IN ('PENDING','RETRY','SENDING'))
     OR EXISTS (SELECT 1 FROM agi_datahub_log_v1.deliveries_delivery
                WHERE publication_status IN ('WAITING','PENDING','RETRY')) THEN
    RAISE EXCEPTION 'Drain validation, publication and mail before upgrading from the modernization branch.';
  END IF;

  UPDATE agi_datahub_log_v1.deliveries_delivery d
  SET validator_version = a.validator_version,
      result_models = a.result_models,
      transfer_version = a.transfer_version,
      legacy_log = 'attempts/' || a.id || '-' || a.version || '/result.log',
      processing_status = CASE WHEN d.publication_status='SUPERSEDED' THEN 'SUPERSEDED'
                               ELSE d.processing_status END
  FROM agi_datahub_log_v1.validation_attempt a
  WHERE a.id = d.attempt_id AND a.delivery_id = d.t_id;

  UPDATE agi_datahub_config_v1.core_apikey
  SET revokedat = CURRENT_TIMESTAMP
  WHERE revokedat IS NULL AND issuance_state IS DISTINCT FROM 'ACTIVE';
  ALTER TABLE agi_datahub_config_v1.core_apikey DROP COLUMN issuance_state;

  DROP TABLE agi_datahub_log_v1.dataset_publication,
      agi_datahub_log_v1.notification, agi_datahub_log_v1.validation_attempt;
  ALTER TABLE agi_datahub_log_v1.deliveries_delivery
      DROP COLUMN operat_id, DROP COLUMN key_id, DROP COLUMN attempt_id,
      DROP COLUMN execution_status, DROP COLUMN validation_status, DROP COLUMN publication_status,
      DROP COLUMN publication_tries, DROP COLUMN publication_next_at,
      ALTER COLUMN notification_status DROP DEFAULT,
      ALTER COLUMN notification_status DROP NOT NULL;
END $$;

-- ili2pg retains old bindings and would suffix a physical name already assigned to a
-- removed or renamed attribute. Re-register the application's model and mappings from dev/.
DELETE FROM agi_datahub_config_v1.t_ili2db_model WHERE filename='SO_AGI_Datahub_20240403.ili';
DELETE FROM agi_datahub_log_v1.t_ili2db_model WHERE filename='SO_AGI_Datahub_20240403.ili';
DELETE FROM agi_datahub_config_v1.t_ili2db_attrname WHERE iliname LIKE 'SO_AGI_Datahub_Config_20240403.%';
DELETE FROM agi_datahub_log_v1.t_ili2db_attrname WHERE iliname LIKE 'SO_AGI_Datahub_Log_20240403.%';
DELETE FROM agi_datahub_log_v1.t_ili2db_classname WHERE sqlname IN ('validation_attempt','dataset_publication','notification');
DELETE FROM agi_datahub_log_v1.t_ili2db_table_prop WHERE tablename IN ('validation_attempt','dataset_publication','notification');
DELETE FROM agi_datahub_log_v1.t_ili2db_column_prop WHERE tablename IN ('validation_attempt','dataset_publication','notification');

DELETE FROM agi_datahub_config_v1.t_ili2db_column_prop
WHERE tablename='core_apikey' AND columnname='issuance_state';
DELETE FROM agi_datahub_log_v1.t_ili2db_column_prop
WHERE tablename='deliveries_delivery' AND columnname IN
    ('operat_id','key_id','attempt_id','execution_status','validation_status',
     'publication_status','publication_tries','publication_next_at');
DELETE FROM agi_datahub_config_v1.t_ili2db_meta_attrs
WHERE ilielement ~ '^SO_AGI_Datahub_(Config|Log)_20240403([.]|$)';
DELETE FROM agi_datahub_log_v1.t_ili2db_meta_attrs
WHERE ilielement ~ '^SO_AGI_Datahub_(Config|Log)_20240403([.]|$)';
DELETE FROM agi_datahub_log_v1.t_ili2db_trafo
WHERE iliname ~ '^SO_AGI_Datahub_Log_20240403[.]Deliveries[.](ValidationAttempt|DatasetPublication|Notification)([.]|$)';
DELETE FROM agi_datahub_log_v1.t_ili2db_inheritance
WHERE thisclass ~ '^SO_AGI_Datahub_Log_20240403[.]Deliveries[.](ValidationAttempt|DatasetPublication|Notification)$'
   OR baseclass ~ '^SO_AGI_Datahub_Log_20240403[.]Deliveries[.](ValidationAttempt|DatasetPublication|Notification)$';

-- The historical setup_gdi.sql (7df15c5) created JobRunr 6 tables with an empty ledger.
INSERT INTO agi_datahub_jobrunr_v1.jobrunr_migrations(id,script,installedon)
SELECT baseline.id, baseline.script, '2026-11-19T00:00:00Z'
FROM (VALUES
    ('4e195090-12f9-5d8c-8168-7519b84f8d8b','v000__create_migrations_table.sql'),
    ('0bc7870c-7ef8-5ef5-9b2d-7da42e169ea3','v001__create_job_table.sql'),
    ('10030cbe-da7c-5c1f-bbe8-e1414181687d','v002__create_recurring_job_table.sql'),
    ('92d1b243-4762-5e8a-8cef-a034c8691934','v003__create_background_job_server_table.sql'),
    ('6b9053f1-a3a2-534b-979c-a11ea50b14a5','v004__create_job_stats_view.sql'),
    ('bd22f47c-b711-5254-9e11-cbe9f382cd0c','v005__update_job_stats_view.sql'),
    ('d7e6e587-5c73-58d8-be63-5d59290ab305','v006__alter_table_jobs_add_recurringjob.sql'),
    ('402846e9-07ee-5c7c-bcae-a62f2a438480','v007__alter_table_backgroundjobserver_add_delete_config.sql'),
    ('7e17aa6e-8502-5127-aa5f-310b90c84273','v008__alter_table_jobs_increase_jobAsJson_size.sql'),
    ('d77a3efe-01f7-5078-b5ba-9527a6f5bc0a','v009__change_jobrunr_job_counters_to_jobrunr_metadata.sql'),
    ('7cea79b3-605d-5dff-b5fa-84d54f79cd01','v010__change_job_stats.sql'),
    ('fa145618-f4db-58b1-916a-0c20b6ede5ae','v011__change_sqlserver_text_to_varchar.sql'),
    ('0ef60090-3e53-5f62-8a42-1143cf7772cb','v012__change_oracle_alter_jobrunr_metadata_column_size.sql'),
    ('382d4e9b-3965-5710-b6db-cdc7b2757f60','v013__alter_table_recurring_job_add_createdAt.sql'),
    ('cc56e90a-0187-54ab-a027-da660b37215a','v014__improve_job_stats.sql'),
    ('cbe6d7fb-ae68-5eb2-a6de-3f10d75efc81','v015__alter_table_backgroundjobserver_add_name.sql')
) AS baseline(id,script)
WHERE NOT EXISTS (
    SELECT 1 FROM agi_datahub_jobrunr_v1.jobrunr_migrations applied
    WHERE applied.script=baseline.script
);

COMMIT;
