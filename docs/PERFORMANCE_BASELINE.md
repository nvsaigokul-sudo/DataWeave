# Performance baseline

Measurements from **verified commands** on Linux cloud agent VM (Java 21, embedded PostgreSQL via Zonky), **2026-10-24 UTC**.

| Metric | Result | Command / evidence |
|--------|--------|-------------------|
| Spring Boot ready (headless smoke) | **1.267 s** (process running 2.171 s) | `./gradlew :desktop:runHeadlessSmoke` log line `Started HeadlessSmokeKt in 1.267 seconds` |
| Headless smoke Gradle wall time | **~9 s** | Same task, BUILD SUCCESSFUL |
| Full test + smoke + Linux `.deb` | **~61 s** wall | `./gradlew test :desktop:runHeadlessSmoke :desktop:packageDeb` |
| Default table page size | **100** rows | `application.properties` `dataweave.query.page-default-size=100` |
| First PG table page (integration) | Sub-second (not UI-isolated) | `PaginationAndTypesIntegrationTest` |

PRD numeric targets on **Windows + owner PostgreSQL + large tables** are **not measured** in this environment.
