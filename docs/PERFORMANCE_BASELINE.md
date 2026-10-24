# Performance baseline

Measurements on **Linux cloud agent VM** (Java 21, Zonky embedded PostgreSQL), **2026-10-24 (Asia/Calcutta)** · Gradle product **`0.3.0`** · branch **`cursor/dataweave-product-v0.3.0-d9c8`** (pre-merge release prep).

| Metric | Result | Command / evidence |
|--------|--------|-------------------|
| Spring Boot ready (headless smoke) | **1.273 s** (process running 2.394 s) | `./gradlew :desktop:runHeadlessSmoke` — `Started HeadlessSmokeKt in 1.273 seconds` |
| Full test + smoke + Linux `.deb` | **~46 s** wall | `./gradlew test :desktop:runHeadlessSmoke :desktop:packageDeb printProductVersion --no-daemon` **BUILD SUCCESSFUL** |
| Linux `.deb` | `dataweave_1.3.0_amd64.deb` — **208,026,464** bytes | `./gradlew :desktop:packageDeb` |
| Linux `.deb` SHA256 | `c15add881aa7d94a94cb0dfc0a0302c079108c8a41de64a4e54a2a0a95e9bd2e` | `sha256sum` on built artifact (jpackage **1.3.0** for product **0.3.0**) |
| Default table page size | **100** rows | `dataweave.query.page-default-size=100` |
| First PG table page (integration) | Sub-second (backend) | `PaginationAndTypesIntegrationTest` |

## Not measured (PRD / Windows targets)

- Cold startup on **Windows packaged MSI**
- Large table scroll/paging on **owner PostgreSQL** with PRD-scale row counts
- Interactive Dataset Builder / grid latency on Windows GUI

Record those on owner hardware before claiming PRD performance **PASS**. Do not substitute Linux CI numbers for Windows UX targets.
