# Regression matrix

Use before each patch release. **v0.2.7** agent run: 2026-10-24.

| ID | Scenario | Expected | v0.2.5 | v0.2.6 | v0.2.7 (auto) | User Windows |
|----|----------|----------|--------|--------|---------------|--------------|
| R01 | Normal GUI launch (Windows CI) | Process alive 15s, Compose in `launch.log` | PASS CI | PASS CI | Pending CI on tag | PENDING |
| R02 | `--headless-smoke` | `HEADLESS_SMOKE_OK` | PASS | PASS | **PASS** | — |
| R03 | `--gui-smoke` | `GUI_SMOKE_OK` | PASS | PASS | **PASS** (Linux deb) | PENDING |
| R04 | Connections page scroll | Full form reachable | **PASS** manual | — | Not re-run | PENDING |
| R05 | Save profile ≠ Connect | Saved shows disconnected until Connect | PASS integration | PASS | **PASS** | PENDING |
| R06 | Explorer after Connect | Tree loads, no infinite Loading | FAIL reported | Fix in 0.2.6 | **PASS** integration | PENDING |
| R07 | Home without connection | Visible Connect / create path | FAIL (0.2.6 UX) | FAIL | **FIX** UI | PENDING |
| R08 | Explorer filter label | Filters objects only when connected | Confusing in 0.2.6 | Confusing | **FIX** | PENDING |
| R09 | DB switch in explorer | Reconnect + refresh | — | PASS integration | **PASS** | PENDING |
| R10 | Spring headless=false for GUI | No `HeadlessException` | PASS | PASS | **PASS** `SpringHeadlessPropertyTest` | — |
| R11 | Query cache key includes SQL | No wrong-row cache hit | PASS | PASS | **PASS** | — |
| R12 | Secrets not in logs | No passwords in stdout/log files | PASS review | PASS | **PASS** | — |

Automated command:

```bash
./gradlew test :desktop:runHeadlessSmoke
```
