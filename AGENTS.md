# Working on Datahub

- Apply Ockham's razor. Delete before replacing. Prefer direct code, fewer persisted states and the existing capabilities of Spring, PostgreSQL, Cayenne and JobRunr.
- Keep one scheduler and one source of truth for the delivery history. Add an abstraction only when a current caller or a concrete correctness boundary needs it.
- Prioritize security and data integrity, then correctness, simplicity, compatibility and convenience. Preserve authorization of the exact delivery target, deny-by-default and public read access to job status and validation logs.
- Keep INTERLIS authoritative. Generate PostgreSQL DDL with ili2pg, import the schema with Cayenne and regenerate Java with cgen. Apply production schema changes explicitly and document how to back up and roll back.
- Keep changes local. Use the Gradle wrapper with JDK 21, run `./gradlew test` and `./gradlew check`, and verify the actual Java 25 container for runtime changes. Use real PostgreSQL and HTTP tests for persistence and security behavior.
- After implementation, run an independent review with local OpenCode 2.x and DeepSeek Flash where available. Apply the established `review-change` and `simplify-change` skills to the complete diff against `origin/main`. Require concrete, reproducible findings ordered by severity, including unnecessary complexity. Fix confirmed findings and review the revised diff again.
- Use existing review tools directly. Keep implementation and review off Codex Cloud and keep secrets out of logs, reviews and commits.
