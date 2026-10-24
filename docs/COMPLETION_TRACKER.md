# DataWeave completion tracker

Last updated: **2026-10-24** (v0.2.5 publish **DONE**; v0.2.4 preserved).

## Live verification (GitHub vs repo)

| Asset | Expected SHA256 | Live check | Status |
|-------|-----------------|------------|--------|
| LFS / media MSI (v0.2.3) | `68eb8524926e26532a2f7345b089fad37380c090c43ed92ef2888d95dfb070f5` | Release + CI Windows smoke [40070764011](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40070764011) | **VERIFIED** |
| v0.2.2 MSI (superseded for GUI) | `35333f603bac…` | Preserved on [v0.2.2 release](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.2) | **SUPERSEDED** for HeadlessException |
| Release v0.2.1 MSI | `6716b5aeccbe3eefbf8669b375e9c0277ebc08cf3a5c8b679b16f979960e9544` | Downloaded 2026-10-24 | **VERIFIED** (unchanged; not deleted) |
| User legacy MSI | `a9c8ab77e653c6f202f58ebd877c526e5c220a712eb97f045b5457923d0f06fb` | Documented only | **SUPERSEDED** — reinstall from LFS |
| raw.githubusercontent MSI | `24f3a23a…` (134 B pointer) | Documented | **INVALID** — use media URL |

## Completed (this cycle)

| Item | Evidence | Commit |
|------|----------|--------|
| PG connection session (Save/Test/Connect/Disconnect/Reconnect) | `ConnectionLifecycleIntegrationTest`; `PostgresConnectionManager` explicit pools | (this push) |
| Workspace UI (top bar, tree, themes, status bar) | `WorkspaceShell`, `DatabaseTreeSidebar` | (this push) |
| Table grid server filter/sort + clipboard copy | `TableBrowseOptions`, `TableDataGrid` | (this push) |
| CSV export streaming (no silent 100k cap) | `TableExportResult` | (this push) |
| Dataset output column persist (V3 migration) | `DatasetOutputDefinitionService` | (this push) |
| **v0.2.3** Spring `headless(false)` for Compose GUI (`startForGui`) | `SpringHeadlessPropertyTest`; user `HeadlessException` root cause | `63b1cfc` |
| Release publish uses **CI Windows MSI** (not stale LFS swap) | `.github/workflows/release.yml` | (this push) |
| Normal GUI CI asserts `launch.log` reaches Compose | `windows-packaged-smoke.ps1` | (this push) |
| Shared Windows packaged smoke script | `scripts/ci/windows-packaged-smoke.ps1` | (this push) |
| `release.yml` Windows GUI install smoke | Same script as `ci.yml` | (this push) |
| Release publish aligns Windows `.msi` with LFS canonical bytes | Publish job `lfs: true` + copy step | (this push) |
| Gradle test + headless smoke | `./gradlew test :desktop:runHeadlessSmoke` PASS | `626372e` baseline |
| M0 features (SQL, CSV, join preview, export) | Integration tests + Compose UI | `32b887c`, `d2d4938` |
| v0.2.2 tag + GitHub Release | [Release v0.2.2](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.2); Windows MSI SHA256 `35333f60…` verified | `4bbcf00` + tag `v0.2.2` |

## Blockers / not verified

| Item | Next action |
|------|-------------|
| User Windows/macOS laptop GUI | Owner runs INSTALL checklist; share `launch-error.log` if needed |
| 20M-row grid / M2 pipeline | Roadmap — not in v0.2.2 scope |
| Code signing / notarization | Future release hardening |

## Release v0.2.5

| Step | Status |
|------|--------|
| Scroll fix integrated (`3c073f8`) | **DONE** |
| Gradle `0.2.5`; jpackage **1.2.5** | **DONE** |
| [V0.2.5_RELEASE.md](./V0.2.5_RELEASE.md) | **DONE** |
| `./gradlew test :desktop:runHeadlessSmoke` | **PASS** (agent 2026-10-24) |
| Tag `v0.2.5` + GitHub Release | **DONE** — [release](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.5) · workflow [40078734530](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40078734530) |
| LFS MSI + SHA256 from live assets | **DONE** — `a67cf1a0…` |
| Windows Connections scroll (manual) | **NOT VERIFIED** |

## Release v0.2.4

| Step | Status |
|------|--------|
| `main` @ `ae43fca` PG session + workspace UI | **DONE** |
| `./gradlew test :desktop:runHeadlessSmoke` | **PASS** (agent 2026-10-24) |
| Linux package `dataweave_1.2.4_amd64.deb` + packaged headless smoke | **PASS** (agent VM) |
| Tag `v0.2.4` + publish | **DONE** — CI [40073615869](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40073615869), Release [40074093471](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40074093471) |
| LFS MSI SHA256 refresh | **DONE** `db19e0c9…` |

## Release v0.2.3

| Step | Status |
|------|--------|
| Root cause documented | Spring Boot default headless → Compose `HeadlessException`; jpackage **Failed to launch JVM** masks Java exit |
| `./gradlew test :desktop:runHeadlessSmoke` | **PASS** (agent) |
| Tag `v0.2.3` + Windows CI smoke + publish | **DONE** — CI [40070764011](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40070764011), Release [40070767809](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40070767809) |
| LFS MSI SHA256 refresh | **DONE** `68eb8524…` |

## Release v0.2.2

| Step | Status |
|------|--------|
| Preconditions (tests, checksums) | **PASS** on agent |
| Tag `v0.2.2` on `main` | **DONE** @ `4bbcf00` |
| `release.yml` workflow | **PASS** [40069176332](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40069176332) |
| v0.2.1 release | **Preserved** (no delete/retag) |

### Publish steps (if tag blocked)

```bash
git checkout main && git pull github main
./gradlew test :desktop:runHeadlessSmoke
./scripts/verify-downloads-checksums.sh
git tag -a v0.2.2 -m "DataWeave v0.2.2"
git push github v0.2.2
# Wait for Release workflow; confirm windows-*.msi SHA256 = 35333f60…
```

## CI reference

| Run | Branch / trigger | Notes |
|-----|------------------|-------|
| [40069173014](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40069173014) | `main` @ `4bbcf00` | ubuntu/windows/macos + downloads-verify **PASS** |
| [40069176332](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40069176332) | tag `v0.2.2` | Release matrix + publish **PASS** |

## Laptop acceptance (Windows)

1. `certutil -hashfile DataWeave-Setup.msi SHA256` → `35333f60…` (from media URL or v0.2.2 release asset after publish).
2. Install; `DataWeave.exe --diagnose` from Program Files.
3. PostgreSQL connection → browse → SQL `SELECT 1` → dataset CSV → export table CSV.
