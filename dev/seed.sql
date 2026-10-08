-- Only synthetic development data; never run against production.
BEGIN;
INSERT INTO agi_datahub_config_v1.core_organisation(t_id,aname,arole,email) VALUES
 (100000,'Demo Supplier','USER','supplier@example.test') ON CONFLICT(t_id) DO NOTHING;
INSERT INTO agi_datahub_config_v1.core_apikey(t_id,apikey,createdat,organisation_r,issuance_state)
 VALUES(100001,'dbe60a63-2bdd-492a-a81d-338e7fdc6abb:$2a$10$xnXSTT7PauseXoTkcRyPD.9XG2V0Oe.xtUGLWwsTqvyVliavRH/Jq',CURRENT_TIMESTAMP,100000,'ACTIVE') ON CONFLICT(t_id) DO NOTHING;
INSERT INTO agi_datahub_config_v1.core_theme(t_id,aname,expected_models)
 VALUES(100002,'Demo','DatahubDemo') ON CONFLICT(t_id) DO UPDATE SET expected_models=EXCLUDED.expected_models;
INSERT INTO agi_datahub_config_v1.core_operat(t_id,aname,theme_r,organisation_r)
 VALUES(100003,'sample',100002,100000) ON CONFLICT(t_id) DO NOTHING;
COMMIT;
