-- Abort with a useful error instead of choosing an arbitrary authorization target.
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM ${configSchema}.core_operat GROUP BY theme_r, aname HAVING count(*) > 1) THEN
  RAISE EXCEPTION 'Duplicate (theme_r, aname) in core_operat. Resolve duplicates before migration.';
 END IF;
END $$;
CREATE UNIQUE INDEX core_operat_theme_name_key ON ${configSchema}.core_operat(theme_r, aname);
ALTER TABLE ${configSchema}.core_apikey ADD issuance_state varchar(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE ${configSchema}.core_apikey ADD CONSTRAINT apikey_issuance_state CHECK (issuance_state IN ('PENDING','ACTIVE','FAILED'));
ALTER TABLE ${configSchema}.core_theme ADD expected_models varchar(2048);
ALTER TABLE ${logSchema}.deliveries_delivery
 ADD operat_id bigint,
 ADD key_id varchar(36),
 ADD attempt_id varchar(36),
 ADD email varchar(512),
 ADD validator_config varchar(512),
 ADD validator_meta_config varchar(512),
 ADD expected_models varchar(2048),
 ADD execution_status varchar(20) NOT NULL DEFAULT 'UNKNOWN',
 ADD validation_status varchar(20),
 ADD publication_status varchar(20) NOT NULL DEFAULT 'LEGACY',
 ADD notification_status varchar(20) NOT NULL DEFAULT 'LEGACY',
 ADD publication_tries integer NOT NULL DEFAULT 0,
 ADD publication_next_at timestamp,
 ADD updated_at timestamp,
 ADD finished_at timestamp,
 ADD artifacts_expired boolean NOT NULL DEFAULT false;
-- Capture technical history before it is decoupled from JobRunr retention.
UPDATE ${logSchema}.deliveries_delivery d SET execution_status = j.state, updated_at = j.updatedat,
 finished_at = CASE WHEN j.state IN ('SUCCEEDED','FAILED','DELETED') THEN j.updatedat END
 FROM ${jobSchema}.jobrunr_jobs j WHERE j.id = d.jobid;
UPDATE ${logSchema}.deliveries_delivery SET validation_status = CASE WHEN isvalid THEN 'DONE' WHEN isvalid = false THEN 'FAILED' END;
CREATE TABLE ${logSchema}.validation_attempt (
 t_id bigserial PRIMARY KEY, id varchar(36) NOT NULL UNIQUE, delivery_id bigint NOT NULL REFERENCES ${logSchema}.deliveries_delivery(t_id),
 dispatch_state varchar(20) NOT NULL DEFAULT 'PENDING', state varchar(20) NOT NULL DEFAULT 'ENQUEUED',
 created_at timestamp NOT NULL, started_at timestamp, finished_at timestamp, heartbeat_at timestamp,
 version bigint NOT NULL DEFAULT 0, validator_version varchar(128), result_models varchar(4096)
);
CREATE INDEX validation_attempt_dispatch_idx ON ${logSchema}.validation_attempt(dispatch_state, state);
CREATE TABLE ${logSchema}.dataset_publication (
 t_id bigserial PRIMARY KEY, operat_id bigint NOT NULL UNIQUE, delivery_id bigint NOT NULL REFERENCES ${logSchema}.deliveries_delivery(t_id)
);
CREATE TABLE ${logSchema}.notification (
 t_id bigserial PRIMARY KEY, id varchar(36) NOT NULL UNIQUE, delivery_id bigint UNIQUE REFERENCES ${logSchema}.deliveries_delivery(t_id),
 recipient varchar(512) NOT NULL, subject varchar(1024) NOT NULL, body text NOT NULL,
 state varchar(20) NOT NULL DEFAULT 'PENDING', tries integer NOT NULL DEFAULT 0,
 next_at timestamp NOT NULL, lease_until timestamp
);
CREATE INDEX notification_pending_idx ON ${logSchema}.notification(state, next_at);
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA ${logSchema} TO datahub;
