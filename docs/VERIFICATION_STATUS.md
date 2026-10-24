# DataWeave verification status

Last updated: **2026-10-24** (v0.2.6 in release; v0.2.5 **published**; v0.2.1–v0.2.4 preserved).

This page summarizes **what we have actually run**, not roadmap intent. For architecture and audit detail see [AUDIT.md](./AUDIT.md).

## Workflow summary

| Workflow | Scope | Status | Evidence |
|----------|--------|--------|----------|
| `ci.yml` (ubuntu) | test, headless smoke, Linux package | **PASS** | [40063956281](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40063956281) |
| `ci.yml` (windows) | test, MSI/EXE, install, JvmDebug.cmd, `--diagnose` / `--headless-smoke` / `--gui-smoke`, normal GUI 15s | **PASS** | same run |
| `ci.yml` (macos-14) | test, headless smoke, macOS package | **PASS** | same run |
| `ci.yml` (`downloads-verify`) | `scripts/verify-downloads-checksums.sh` | **PASS** | same run |
| `release.yml` @ v0.2.1 | Linux/Windows/macOS artifacts | **PASS** (build) | [39998843720](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39998843720) — **no** Windows GUI install smoke (fixed on `main` for v0.2.2+) |
| `release.yml` @ v0.2.5 | Linux/Windows/macOS artifacts + publish | **PASS** | [40078734530](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40078734530) |
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
| Packaged `--diagnose` | **PASS** (`.deb` install smoke) | **PASS** | **NOT TESTED** in CI |
| Packaged `--headless-smoke` | **PASS** | **PASS** | **NOT TESTED** in CI |
| Packaged `--gui-smoke` | **PASS** (Xvfb) | **PASS** (Access Bridge + bad CWD) | **NOT TESTED** in CI |
| Normal GUI alive 15s (no smoke flags) | **NOT TESTED** in CI | **PASS** (install root, `%USERPROFILE%`, Start Menu CWD) | **NOT TESTED** in CI |
| Pixel / manual connection UX | **NOT TESTED** | **NOT TESTED** | **NOT TESTED** |

## PostgreSQL end-to-end

| Check | Status | Notes |
|-------|--------|--------|
| Embedded PG in integration tests | **PASS** | Zonky; not user production DB |
| Multi-page + NULLs + types fixture | **PASS** | `PaginationAndTypesIntegrationTest` (120 rows, 50/page, NUMERIC/DATE/BOOLEAN) |
| Docker PG on agent VM | **BLOCKED** | `docker` not installed |
| Connection lifecycle (save ≠ connect, bad creds, unreachable) | **PASS** | `ConnectionLifecycleIntegrationTest` |
| GUI connection-first workspace | **NOT TESTED** on laptop | Compose `WorkspaceShell`; Linux CI GUI not re-run this cycle |
| GUI “add connection” flow | **PARTIAL** | Separate Save/Test/Connect/Disconnect in UI; laptop **NOT TESTED** |
| **v0.2.5** Connections page vertical scroll | **PASS** (manual, user Windows) | User verified launch + scroll 2026-10-24; fix `3c073f8` |
| **v0.2.6** Explorer lazy tree after Connect | **NOT VERIFIED** (manual) | P0 fix: no infinite Loading; Linux integration + headless smoke **PASS** |
| **v0.2.6** UI modernization / startup timing | **PARTIAL** | `StartupTiming` in launch.log; laptop timing **NOT VERIFIED** |

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
| LFS + [media.githubusercontent.com MSI](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi) (v0.2.5) | `a67cf1a0b93be83a1b9662ac72c351497d8e2e926914bdf150b66139254bd32d` | 188067924 | Release [40078734530](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40078734530) — live download verified 2026-10-24 |
| v0.2.4 MSI (superseded) | `db19e0c98c6d4efc1b130a6b5333125dba62088afb9f3b7c47e08c6ddfb2d419` | (see release) | Preserved on [v0.2.4 release](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.4) |
| v0.2.3 MSI (superseded) | `68eb8524…` | Preserved on [v0.2.3 release](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.3) | **SUPERSEDED** by v0.2.4 feature release |
| v0.2.2 MSI | `35333f603bac8180316934b299623227d2ce35dea5dbf767af92fea45a4c5323` | 187727956 | GUI HeadlessException — use v0.2.3 |
| `raw.githubusercontent.com/.../DataWeave-Setup.msi` | `24f3a23a8d46760578818e9ef2ab62102f558e9d3d9c163247fa454bd3046f17` | 134 | LFS pointer — **invalid installer** |
| Legacy LFS MSI (user report) | `a9c8ab77e653c6f202f58ebd877c526e5c220a712eb97f045b5457923d0f06fb` | ~167 MB | Pre-reset; superseded by `35333f60…` |
| Release `windows-DataWeave-1.2.1.msi` (v0.2.1 / **Latest**) | `6716b5aeccbe3eefbf8669b375e9c0277ebc08cf3a5c8b679b16f979960e9544` | 187499257 | Release build only — **no** install/GUI smoke in `release.yml` |
| Release `linux-dataweave_1.2.1_amd64.deb` (v0.2.1) | `4e30d1482302f78c43fae3b8d51721b4c00b5190eafc664fe50cd410dcddc0c1` | (see release `SHA256SUMS.txt`) | Release build — local install smoke uses VM rebuild below |
| **This VM** `dataweave_1.2.1_amd64.deb` (`b90dcb0` tree) | `3832dc3a826743d1877563a22498b8af27c2b9fe5389050802018bda9339640f` | (see build output) | `--diagnose`, `--headless-smoke` **PASS** 2026-10-24 audit |

Different MSI hashes reflect **different build runs**; README explains when to prefer the LFS MSI (CI GUI-tested modules: `java.desktop`, `jdk.accessibility`).

## Windows GUI headless regression (v0.2.3)

| Check | Status | Notes |
|-------|--------|--------|
| `SpringHeadlessPropertyTest` (`startForGui` → `java.awt.headless=false`) | **PASS** | Linux agent |
| Packaged normal GUI reaches Compose in `launch.log` | **PASS** | Windows CI [40070764011](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40070764011) |
| User laptop with v0.2.2 `HeadlessException` | **FIX EXPECTED** | Requires v0.2.3 MSI (new SHA256) |

**Root cause (v0.2.2 and earlier):** `DesktopSession.start()` used Spring Boot’s default headless mode before Compose `application { }`, so AWT reported headless on Windows and Skiko/Compose threw `HeadlessException` (`getGlobalDensity` / `getDefaultScreenDevice`). `--gui-smoke` could pass when it set headless false manually; normal launch did not. jpackage’s **Failed to launch JVM** is the generic wrapper for this Java-side exit.

## Windows JVM / laptop troubleshooting

CI proves the **current** Windows package recipe (explicit jlink modules in `desktop/build.gradle.kts`, install smoke in `ci.yml`) launches JVM and keeps GUI alive on `windows-latest`. That does **not** prove every user machine.

If you see **“Failed to launch JVM”** or an immediate exit on a laptop:

1. Install from [LFS MSI](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi) (CI-tested) or a fresh CI artifact.
2. Run `DataWeave.exe --diagnose` from `C:\Program Files\DataWeave`.
3. Run [scripts/diagnostics/DataWeave-Windows-Diagnostics.ps1](../scripts/diagnostics/DataWeave-Windows-Diagnostics.ps1) with `-RunPackagedDiagnose` (optional `-RunSmokeTests`).
4. Share `%LOCALAPPDATA%\DataWeave\launch-error.log` if present — **do not** paste passwords.

## Release readiness (current slice)

**Ready** for: installable desktop app, PostgreSQL connection/browse/pagination for the implemented UI, automated CI on `main`, Windows CI GUI/JVM launch smoke.

**Not ready / not claimed:** dataset builder depth (M1 partial), large-scale grid / 20M-row performance, code signing, notarized macOS builds, **user Windows/macOS laptop verification (launch criterion #1)**.

Downloads layout: `downloads/{windows,linux,macos}/` with `./scripts/verify-downloads-checksums.sh` in CI (`downloads-verify` job).

## Product vs roadmap

| Roadmap item | Classification |
|--------------|----------------|
| M0 — connections, metadata browse, paginated grid, SQL editor + history, table CSV export | **Implemented & tested** (backend integration tests + headless; Windows CI packaged GUI for browse path) |
| M1 — CSV ingest, virtualized preview | **Partial** — CSV register + paginated preview + column subset preview; no 20M-row virtualization |
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
