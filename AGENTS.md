# AGENTS.md — Spring Boot 3.x (Java 21, Gradle, Spring Security)

## Purpose
This file defines the working rules for coding agents (e.g., Codex) in this repository. Follow it strictly to keep changes safe, reviewable, and consistent.

## Tech baseline
- Java: 21
- Framework: Spring Boot 3.x
- Build: Gradle (use wrapper)
- Security: Spring Security
- Tests: JUnit 5

## Ground rules (must follow)
- Keep changes small and scoped to the request. Avoid repo-wide refactors/renames/reformatting.
- Prefer existing patterns, packages, and utilities in this codebase over introducing new abstractions.
- Do not add/remove dependencies unless necessary; explain why and keep it minimal.
- Never modify or commit secrets (tokens, credentials, private keys). Use `.env.example` or docs if needed.
- If requirements are ambiguous, write a short plan (3–6 bullets) before changing code.
- Preserve public APIs and security behavior unless explicitly asked to change them.

## Commands (use Gradle wrapper)
Run from the repository root unless stated otherwise.

### Build & verification
- Build: `./gradlew build`
- Fast checks: `./gradlew test`
- Full checks (if configured): `./gradlew check`

### Running the app
- Run (if Spring Boot plugin configured): `./gradlew bootRun`

### Targeted testing
- Single test class:
  - `./gradlew test --tests com.example.SomeTest`
- Single test method:
  - `./gradlew test --tests com.example.SomeTest.someMethod`

## Project structure (typical)
- Production code: `src/main/java/...`
- Resources: `src/main/resources/...`
- Tests: `src/test/java/...`

## Spring Boot conventions
- Prefer constructor injection.
- Prefer `@ConfigurationProperties` for structured configuration.
- Avoid reading environment variables directly in business logic.
- Keep configuration explicit; avoid magic defaults unless already established in the repo.
- Use SLF4J logging; avoid excessive debug/trace logs and never log secrets.

## Spring Security conventions (high priority)
Security changes are high-impact. Be conservative and explicit.

### General rules
- Do not weaken authentication/authorization, CSRF, CORS, session policies, or password handling unless explicitly requested.
- Prefer the Spring Security 6 / Boot 3 style:
  - Define `SecurityFilterChain` beans (avoid deprecated `WebSecurityConfigurerAdapter`).
- Keep authorization rules readable and minimal:
  - Prefer route grouping and clear matchers.
- Avoid broad permit-all rules (e.g., `/**`) unless explicitly required.

### CORS / CSRF
- Treat CORS and CSRF as separate concerns; do not disable CSRF by default.
- If enabling cross-origin for a frontend, scope origins/methods/headers tightly.
- If you must adjust CSRF:
  - Prefer ignoring specific endpoints rather than global disablement,
  - Document the reasoning in code comments.

### Passwords / users / tokens
- Use `PasswordEncoder` (e.g., BCrypt) consistently; never store plaintext passwords.
- Never introduce custom crypto unless requested; prefer standard Spring Security components.
- If JWT/OAuth2 exists, keep token validation strict and consistent with existing config.

### Method security
- If the project uses method security (`@PreAuthorize`, etc.), keep it consistent.
- Do not add method security annotations broadly without confirming intent.

## Testing conventions (JUnit 5)
- New/changed behavior requires tests.
- Prefer fast unit tests; add integration tests only when necessary.
- Keep tests deterministic:
  - Avoid `Thread.sleep`.
  - Prefer injectable `Clock`, stubs/mocks, or time control utilities.
- For Spring tests:
  - Prefer slice tests (`@WebMvcTest`, etc.) where possible.
  - Use `@SpringBootTest` only when needed.

### Security testing
When touching security rules, add/adjust tests:
- Use `spring-security-test` helpers when available (`@WithMockUser`, request post-processors).
- Include at least:
  - one authorized access case,
  - one forbidden/unauthorized case,
  - and any special-case endpoint behavior (e.g., health, login, public assets).

## Change hygiene
- Keep diffs reviewable: avoid unrelated formatting changes.
- Update docs/config when behavior changes.
- Do not leave commented-out code, debug prints, or temporary endpoints.

## Definition of Done (before finishing)
- [ ] `./gradlew test` passes
- [ ] `./gradlew check` passes (if configured)
- [ ] Security behavior is not weakened unintentionally
- [ ] New/changed logic is covered by tests (including security tests when relevant)
- [ ] No secrets added; no sensitive info logged
- [ ] Changes are scoped, clean, and consistent with repo patterns

