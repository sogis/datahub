-- JobRunr 8.8.2 migration v016, taken from the official artifact.
DROP VIEW ${jobSchema}.jobrunr_jobs_stats;
CREATE VIEW ${jobSchema}.jobrunr_jobs_stats
AS
with job_stat_results AS (SELECT state, count(*) AS count
    FROM ${jobSchema}.jobrunr_jobs
    GROUP BY state
)
SELECT coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results), 0)                            AS total,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'AWAITING'), 0)   AS awaiting,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'SCHEDULED'), 0)  AS scheduled,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'ENQUEUED'), 0)   AS enqueued,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'PROCESSING'), 0) AS processing,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'PROCESSED'), 0)  AS processed,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'FAILED'), 0)     AS failed,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'SUCCEEDED'), 0)  AS succeeded,
       coalesce((SELECT cASt(cASt(value AS char(10)) AS decimal(10, 0))
                 FROM ${jobSchema}.jobrunr_metadata jm
                 WHERE jm.id = 'succeeded-jobs-counter-cluster'),
                0)                                                                                        AS allTimeSucceeded,
       coalesce((SELECT sum(job_stat_results.count) FROM job_stat_results WHERE state = 'DELETED'), 0)    AS deleted,
       (SELECT count(*) FROM ${jobSchema}.jobrunr_backgroundjobservers)                                                AS nbrOfBackgroundJobServers,
       (SELECT count(*) FROM ${jobSchema}.jobrunr_recurring_jobs)                                                      AS nbrOfRecurringJobs;
INSERT INTO ${jobSchema}.jobrunr_migrations(id,script,installedon) SELECT '72e2484c-1df0-5634-a0a8-39cbec5fd957','v016__alter_jobs_stats_add_awaiting_jobs.sql','2026-10-08T00:00:00Z' WHERE NOT EXISTS(SELECT 1 FROM ${jobSchema}.jobrunr_migrations WHERE script='v016__alter_jobs_stats_add_awaiting_jobs.sql');
