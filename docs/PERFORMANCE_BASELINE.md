# Performance baseline

Environment: Linux cloud agent VM, embedded PostgreSQL (Zonky), Java 21, **2026-10-24 (UTC)**.

| Metric | Measurement | Command / source |
|--------|-------------|------------------|
| Spring Boot ready (headless smoke) | ~1.5–2.0 s after JVM start | `:desktop:runHeadlessSmoke` log `Started HeadlessSmokeKt in …` |
| Headless smoke end-to-end | ~13 s wall (includes embedded PG boot) | `./gradlew :desktop:runHeadlessSmoke` |
| Default table page size | 100 rows | `dataweave.query.page-default-size=100` |
| First table page (integration) | Not isolated in UI | Use Windows manual + app status bar ms in Data grid |

Targets from PRD are not yet fully benchmarked on Windows hardware with user PostgreSQL.
