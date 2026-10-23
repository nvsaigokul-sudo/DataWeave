# Performance budget

**Policy:** No published numeric targets until measured on reference Windows hardware (see PRD Appendix A). Session 1 establishes instrumentation hooks only.

## Candidate metrics (UNVERIFIED — not yet measured)

| Metric | Measurement method | Owner milestone |
|--------|-------------------|-----------------|
| Cold start to interactive shell | Process timestamp → first frame | M0 follow-up |
| Project list query (100 rows) | JDBC timing log | M1 |
| CSV sample 10k rows → grid ready | Trace span | M1 |
| Pipeline run overhead | Run metadata timestamps | M2 |

## Architecture allowances

- UI thread must not block on JDBC or file I/O.
- Future preview uses bounded samples before full scans.
- Future execution uses streaming transforms with spill directory under `%LOCALAPPDATA%/DataWeave`.

When measurements exist, record commit SHA, hardware, dataset, and p50/p95 in this file.
