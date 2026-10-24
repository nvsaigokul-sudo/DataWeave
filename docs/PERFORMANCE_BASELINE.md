# Performance baseline

Measurements from **verified commands** on Linux cloud agent VM (Java 21, embedded PostgreSQL via Zonky), **2026-10-24 (Asia/Calcutta)** · commit **`3126f14`**.

| Metric | Result | Command / evidence |
|--------|--------|-------------------|
| Spring Boot ready (headless smoke) | **1.213 s** (process running 2.097 s) | `./gradlew :desktop:runHeadlessSmoke` — `Started HeadlessSmokeKt in 1.213 seconds` |
| Full test + smoke + Linux `.deb` | **~46 s** wall | `./gradlew test :desktop:runHeadlessSmoke :desktop:packageDeb --no-daemon` BUILD SUCCESSFUL |
| Linux `.deb` SHA256 | `c15add881aa7d94a94cb0dfc0a0302c079108c8a41de64a4e54a2a0a95e9bd2e` | `desktop/build/compose/binaries/main/deb/dataweave_1.3.0_amd64.deb` (208,026,464 bytes) |
| Default table page size | **100** rows | `application.properties` `dataweave.query.page-default-size=100` |
| First PG table page (integration) | Sub-second (not UI-isolated) | `PaginationAndTypesIntegrationTest` |

PRD numeric targets on **Windows + owner PostgreSQL + large tables** are **not measured** in this environment. **Windows MSI SHA256** must come from `windows-latest` CI or local Windows build — not fabricated here.
