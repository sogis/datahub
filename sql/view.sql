CREATE OR REPLACE VIEW ${DB_SCHEMA_LOG}.v_jobresponse AS
WITH queue_position AS 
(
    SELECT 
        j.id, 
        createdat,
        ROW_NUMBER() OVER (ORDER BY createdat) AS queueposition
    FROM 
        ${DB_SCHEMA_JOBRUNR}.jobrunr_jobs AS j
    WHERE 
        j.state = 'ENQUEUED'
)
SELECT 
    d.jobid,
    (j.createdat AT TIME ZONE 'UTC') AT TIME ZONE 'Europe/Zurich' AS createdat,
    (j.updatedat AT TIME ZONE 'UTC') AT TIME ZONE 'Europe/Zurich' AS updatedat,
    CASE 
        WHEN state = 'SUCCEEDED' THEN 'SUCCEEDED'
        ELSE state
    END as status, 
    queue_position.queueposition AS queueposition,
    d.operat AS operat,
    d.theme AS theme,
    d.organisation AS organisation,
    CASE 
        WHEN isvalid IS TRUE THEN CAST('DONE' AS VARCHAR(512)) 
        WHEN isvalid IS FALSE THEN CAST('FAILED' AS VARCHAR(512))
        ELSE CAST(NULL AS VARCHAR(512))
    END AS validationstatus
FROM 
    ${DB_SCHEMA_JOBRUNR}.jobrunr_jobs AS j
    LEFT JOIN ${DB_SCHEMA_LOG}.deliveries_delivery AS d 
    ON j.id = d.jobid 
    LEFT JOIN queue_position 
    ON queue_position.id = j.id
;