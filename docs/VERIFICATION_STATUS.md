# DataWeave verification status

Last updated: **2026-10-24** (v0.2.2 published; CI [40069173014](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40069173014); Release [40069176332](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40069176332); release Windows MSI matches LFS `35333f60…`).

This page summarizes **what we have actually run**, not roadmap intent. For architecture and audit detail see [AUDIT.md](./AUDIT.md).

## Workflow summary

| Workflow | Scope | Status | Evidence |
|----------|--------|--------|----------|
| `ci.yml` (ubuntu) | test, headless smoke, Linux package | **PASS** | [40063956281](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40063956281) |
| `ci.yml` (windows) | test, MSI/EXE, install, JvmDebug.cmd, `--diagnose` / `--headless-smoke` / `--gui-smoke`, normal GUI 15s | **PASS** | same run |
| `ci.yml` (macos-14) | test, headless smoke, macOS package | **PASS** | same run |
| `ci.yml` (`downloads-verify`) | `scripts/verify-downloads-checksums.sh` | **PASS** | same run |
| `release.yml` @ v0.2.1 | Linux/Windows/macOS artifacts | **PASS** (build) | [39998843720](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39998843720) — **no** Windows GUI install smoke (fixed on `main` for v0.2.2+) |
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
| LFS + [media.githubusercontent.com MSI](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi) (README primary) | `35333f603bac8180316934b299623227d2ce35dea5dbf767af92fea45a4c5323` | 187727956 | Re-fetched 2026-10-24 audit; CI matrix [40063956281](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40063956281) |
| `raw.githubusercontent.com/.../DataWeave-Setup.msi` | `24f3a23a8d46760578818e9ef2ab62102f558e9d3d9c163247fa454bd3046f17` | 134 | LFS pointer — **invalid installer** |
| Legacy LFS MSI (user report) | `a9c8ab77e653c6f202f58ebd877c526e5c220a712eb97f045b5457923d0f06fb` | ~167 MB | Pre-reset; superseded by `35333f60…` |
| Release `windows-DataWeave-1.2.1.msi` (v0.2.1 / **Latest**) | `6716b5aeccbe3eefbf8669b375e9c0277ebc08cf3a5c8b679b16f979960e9544` | 187499257 | Release build only — **no** install/GUI smoke in `release.yml` |
| Release `linux-dataweave_1.2.1_amd64.deb` (v0.2.1) | `4e30d1482302f78c43fae3b8d51721b4c00b5190eafc664fe50cd410dcddc0c1` | (see release `SHA256SUMS.txt`) | Release build — local install smoke uses VM rebuild below |
| **This VM** `dataweave_1.2.1_amd64.deb` (`b90dcb0` tree) | `3832dc3a826743d1877563a22498b8af27c2b9fe5389050802018bda9339640f` | (see build output) | `--diagnose`, `--headless-smoke` **PASS** 2026-10-24 audit |

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
