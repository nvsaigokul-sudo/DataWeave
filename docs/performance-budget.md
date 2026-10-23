# Performance budget

## Measured (Linux CI, 2026-10-23)

| Metric | Value | Command / notes |
|--------|-------|-----------------|
| Spring context startup (headless smoke) | **~1.1 s** | Log line `Started HeadlessSmokeKt in 1.1 seconds` from `./gradlew runHeadlessSmoke` |
| Backend unit+integration tests | **4 tests**, PASS | `./gradlew :backend:test` |

No Windows cold-start or grid latency numbers yet — **do not extrapolate SLAs** until recorded on Windows 11 reference hardware (PRD Appendix A).

## Architecture allowances

- UI dispatches JDBC on background dispatchers / executor pool (`dataweave.query.pool-size`, default 4).
- Table grid fetches **≤500 rows** per page (default 100).
- Caffeine bounds: metadata 500 entries / 30 min; data pages 200 / 5 min; dataset preview 50 / 2 min; query results 100 / 1 min (SELECT-only).

Update this file after Windows measurements.
