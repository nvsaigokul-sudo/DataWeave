# DataWeave interactive control inventory

**Version target:** 0.2.9  
**Last updated:** 2026-10-24  

Legend: **PASS** = wired and covered by automated test or code review; **FIX** = repaired in 0.2.7 prep; **MANUAL** = requires Windows GUI verification; **N/I** = not implemented.

## Shell / navigation

| Control | Screen | Intended | Status |
|---------|--------|----------|--------|
| Home / Connections / Data / SQL / Datasets / Projects tabs | Top bar | Switch workspace section | PASS |
| Dark theme switch | Top bar | Toggle theme + persist | PASS (`UserPreferencesTest`) |
| Status bar connection label | Footer | Show live session state | PASS |
| Explorer Refresh | Sidebar | Reload metadata tree | PASS + MANUAL |
| Explorer Connect | Sidebar | Connect saved profile | PASS + MANUAL |
| Explorer filter field | Sidebar | Filter object names when connected | FIX (label "Filter objects"; hidden when disconnected) |

## Home (`HomeConnectionHub`)

| Control | Intended | Status |
|---------|----------|--------|
| Create connection | Open Connections (new form) | FIX |
| Open Connections / Manage all | Navigate to Connections | PASS |
| Connect (profile row) | `facade.connectConnection` | PASS + integration |
| Edit (profile row) | Open Connections with profile loaded | FIX |
| Connect / Retry / Disconnect (active card) | Session lifecycle | PASS |
| Profile row select | Set active profile id (persisted) | PASS |

## Connections

| Control | Intended | Status |
|---------|----------|--------|
| Save | Persist profile; stay disconnected | PASS (`ConnectionLifecycleIntegrationTest`) |
| New | Reset full form | FIX |
| Edit | Load profile into form | PASS |
| Test | Probe JDBC without persistent connect | PASS |
| Connect | Open pool + callback to workspace | PASS |
| Disconnect | Close pool | PASS |
| Reconnect | Disconnect + connect | PASS |
| Refresh metadata | Invalidate schema cache | PASS |
| Delete | Confirm + remove profile | FIX (confirmation dialog) |
| Advanced SSL / timeout | Expand optional fields | PASS |
| Start help (Windows) | Show service start instructions | FIX (was no-op button) |
| Explorer credential recovery | Inline password + Save/Test/Connect when vault empty | FIX (v0.2.9) |
| Edit connection (explorer) | Open Connections with profile loaded | FIX |

## Database Explorer tree

| Control | Intended | Status |
|---------|----------|--------|
| Expand database / schema / table | Lazy load children | PASS + integration |
| Switch to database | `switchConnectionDatabase` + refresh | PASS + integration |
| Retry on error | Retry load or connect | PASS |
| Select table | Open Data grid | PASS + MANUAL |
| Column expansion | List columns | PASS + integration |

## Data grid

| Control | Intended | Status |
|---------|----------|--------|
| Previous / Next | Server pagination | PASS + MANUAL |
| Sort column header | Re-query sorted | MANUAL |
| Filter field | Server filter | MANUAL |
| Copy cell | Clipboard | MANUAL |
| Export CSV | Stream export | PASS backend + MANUAL |

## SQL editor

| Control | Intended | Status |
|---------|----------|--------|
| Connection chips | Select connection | FIX (syncs with active connection) |
| Run | Execute SELECT | PASS + MANUAL |
| Clear | Reset editor/results | FIX |
| History row click | Restore SQL | PASS + MANUAL |

## Datasets

| Control | Intended | Status |
|---------|----------|--------|
| Project / connection chips | Scope selection | FIX (active connection sync) |
| Column add/remove/reorder | Output mapping | MANUAL |
| Preview / join / CSV | Backend preview APIs | PASS backend + MANUAL |
| Save definition | Persist V3 metadata | MANUAL |

## Projects

| Control | Intended | Status |
|---------|----------|--------|
| Create project | SQLite insert | PASS |
| Delete / rename project | — | N/I (no backend API) |

## Automated coverage

- `./gradlew test` — connection lifecycle, metadata explorer, pagination, cache, headless property
- `./gradlew :desktop:runHeadlessSmoke`
- Compose UI automation — **not** in repo; manual Windows pass required for controls marked MANUAL
