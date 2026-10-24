# DataWeave verification status

Last updated: **2026-10-24** (launch hardening pass; see CI run after push to `main`).

This page summarizes **what we have actually run**, not roadmap intent. For architecture and audit detail see [AUDIT.md](./AUDIT.md).

## Workflow summary

| Workflow | Scope | Status | Evidence |
|----------|--------|--------|----------|
| `ci.yml` (ubuntu) | test, headless smoke, Linux package | **PASS** | [Run 40061538019](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40061538019) |
| `ci.yml` (windows) | test, MSI/EXE, install, `--diagnose` / `--headless-smoke` / `--gui-smoke`, normal GUI alive 15s (×3 CWDs) | **PASS** | same run — `GUI smoke OK`, `Process still alive after 15s` |
| `ci.yml` (macos-14) | test, headless smoke, macOS package | **PASS** | same run |
| `release.yml` @ v0.2.1 | Linux/Windows/macOS artifacts | **PASS** (build) | [39998843720](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39998843720) — **no** Windows GUI install smoke |
| Local Gradle (Linux VM) | `./gradlew test :desktop:runHeadlessSmoke` | **PASS** | agent 2026-10-24 |
| Linux `.deb` install smoke | `/opt/dataweave/bin/DataWeave` CLI modes | **PASS** | packaged `@ baa193c` tree |
| Linux packaged `--gui-smoke` (Xvfb) | Compose window opens | **PASS** | agent 2026-10-24 — `GUI_SMOKE_OK` |
| Linux interactive UI (connections wizard) | Full Compose UX | **NOT TESTED** | no manual session |
| User Windows/macOS laptop | Manual install | **NOT TESTED** | cloud constraint — use diagnostics script below |

Prior CI with same Windows GUI matrix: [40061037014](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40061037014) @ `20b0952`.

## Cross-platform CI matrix (dimensions)

| Dimension | ubuntu-latest | windows-latest | macos-14 |
|-----------|---------------|----------------|----------|
| `./gradlew test` | **PASS** | **PASS** | **PASS** |
| `:desktop:runHeadlessSmoke` (source tree) | **PASS** | **PASS** | **PASS** |
| Native installer build | `.deb` + AppImage **PASS** | `.msi` + `.exe` **PASS** | `.dmg` + `.pkg` **PASS** |
| Packaged `--diagnose` | **NOT TESTED** in CI | **PASS** | **NOT TESTED** in CI |
| Packaged `--headless-smoke` | **NOT TESTED** in CI | **PASS** | **NOT TESTED** in CI |
| Packaged `--gui-smoke` | **NOT TESTED** in CI | **PASS** (Access Bridge + bad CWD) | **NOT TESTED** in CI |
| Normal GUI alive 15s (no smoke flags) | **NOT TESTED** in CI | **PASS** (install root, `%USERPROFILE%`, Start Menu CWD) | **NOT TESTED** in CI |
| Pixel / manual connection UX | **NOT TESTED** | **NOT TESTED** | **NOT TESTED** |

## PostgreSQL end-to-end

| Check | Status | Notes |
|-------|--------|--------|
| Embedded PG in integration tests | **PASS** | Zonky; not user production DB |
| Multi-page + NULLs + types fixture | **PASS** | `PaginationAndTypesIntegrationTest` (120 rows, 50/page, NUMERIC/DATE/BOOLEAN) |
| Docker PG on agent VM | **BLOCKED** | `docker` not installed |
| GUI “add connection” flow | **NOT TESTED** | backend path covered by tests + headless smoke |

## Security / cache (recent fixes)

| Item | Status |
|------|--------|
| Read-only query cache keys include SQL text | **PASS** on `main` |
| Table page cache keys include offset/limit/schema version | **PASS** (existing) |
| Dataset preview cache keys include offset/limit | **PASS** (regression: `DatasetPreviewCacheIntegrationTest`) |
| Caffeine caches bounded (`maximumSize` in `CacheConfig`) | **PASS** |
| Table fetch page size capped at 500; stale async requests cancelled | **PASS** (`TableDataService`) |
| Compose table grid uses `LazyColumn` (page-sized rows) | **PASS** — **NOT TESTED** for 20M-row / large heap |

## Download artifacts (checksums & sizes)

| Artifact | SHA256 | Size (bytes) | Test evidence |
|----------|--------|--------------|---------------|
| LFS `downloads/windows/DataWeave-Setup.msi` (README primary Windows link) | `b613ec64d2c30abe613ac2ec821cc0c4443c70c539023b481b33118666dd886e` | 187499256 | Built @ `2ac8c22`; CI GUI matrix [39996985661](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39996985661) |
| Release `windows-DataWeave-1.2.1.msi` (v0.2.1) | `6716b5aeccbe3eefbf8669b375e9c0277ebc08cf3a5c8b679b16f979960e9544` | 187499257 | Release build only — **no** install/GUI smoke in `release.yml` |
| Release `linux-dataweave_1.2.1_amd64.deb` (v0.2.1) | `4e30d1482302f78c43fae3b8d51721b4c00b5190eafc664fe50cd410dcddc0c1` | (see release `SHA256SUMS.txt`) | Release build — local install smoke uses VM rebuild below |
| **This VM** `dataweave_1.2.1_amd64.deb` (`baa193c` tree) | `38be7af0303fb51cd97a3af1d3882cca8aba0f222949d48e47364cb9cf5b34f6` | 173135728 | `--diagnose`, `--headless-smoke`, Xvfb `--gui-smoke` **PASS** |

Different MSI hashes reflect **different build runs**; README explains when to prefer the LFS MSI (CI GUI-tested modules: `java.desktop`, `jdk.accessibility`).

## Windows JVM / laptop troubleshooting

CI proves the **current** Windows package recipe (explicit jlink modules in `desktop/build.gradle.kts`, install smoke in `ci.yml`) launches JVM and keeps GUI alive on `windows-latest`. That does **not** prove every user machine.

If you see **“Failed to launch JVM”** or an immediate exit on a laptop:

1. Install from [LFS MSI](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi) (CI-tested) or a fresh CI artifact.
2. Run `DataWeave.exe --diagnose` from `C:\Program Files\DataWeave`.
3. Run [scripts/diagnostics/DataWeave-Windows-Diagnostics.ps1](../scripts/diagnostics/DataWeave-Windows-Diagnostics.ps1) with `-RunPackagedDiagnose` (optional `-RunSmokeTests`).
4. Share `%LOCALAPPDATA%\DataWeave\launch-error.log` if present — **do not** paste passwords.

## Release readiness (current slice)

**Ready** for: installable desktop app, PostgreSQL connection/browse/pagination for the implemented UI, automated CI on `main`, Windows CI GUI/JVM launch smoke.

**Not ready / not claimed:** dataset builder (M1), large-scale grid / 20M-row performance, code signing, notarized macOS builds, release-workflow parity with Windows GUI smoke, **user Windows/macOS laptop verification (launch criterion #1)**.

Downloads layout: `downloads/{windows,linux,macos}/` with `./scripts/verify-downloads-checksums.sh` in CI (`downloads-verify` job).

## Product vs roadmap

| Roadmap item | Classification |
|--------------|----------------|
| M0 — connections, metadata browse, paginated grid | **Implemented & tested** (backend + headless; Windows CI packaged GUI) |
| M1 — CSV ingest, virtualized preview | **Not implemented** |
| M2 — pipeline designer | **Not implemented** |
| M3 — Windows packaging | **Implemented** (MSI/EXE; CI smoke); code signing **not done** |
| M4 — optional sync | **Not implemented** |

See [IMPLEMENTATION_ROADMAP.md](./IMPLEMENTATION_ROADMAP.md).

## How to reproduce (developers)

```bash
./gradlew test :desktop:runHeadlessSmoke
./gradlew :desktop:packageDeb
sudo dpkg -i desktop/build/compose/binaries/main/deb/dataweave_1.2.1_amd64.deb
/opt/dataweave/bin/DataWeave --headless-smoke
xvfb-run -a /opt/dataweave/bin/DataWeave --gui-smoke
```

Internal agent report: Project store `internal/dataweave-final-e2e-report-2026-10-24.md`.
