# Windows release acceptance (v0.3.0)

**Publication gate:** Owner manual scenarios **#1–#9** below must be recorded **PASS** (or approved **PARTIAL** with written waiver) before **`v0.3.0`** GitHub Release. Automated CI smoke alone is **not** sufficient.

## Version convention

- Gradle / GitHub product: **`0.3.0`**
- Windows installer files: **`DataWeave-1.3.0.msi`**, **`DataWeave-1.3.0.exe`**
- Apps & Settings display: **1.3.0** (jpackage mapping — see [VERSIONING.md](./VERSIONING.md))

## Build MSI/EXE

### GitHub Actions (recommended)

1. Use commit on **`main`** (after merge) or **`cursor/dataweave-product-v0.3.0-d9c8`** with Gradle **`0.3.0`** — **not** `main` @ `3dbb41c` (v0.2.9 / **1.2.9** installers).
2. Workflow **Windows test installer (no release)** or **Release** (on `v0.3.0` tag).
3. Download **`TEST-windows-msi-<sha>`** or release assets; `<sha>` must match the intended commit.
4. Record **Report version and SHA256** (Gradle, jpackage, MSI/EXE metadata, SHA256).

### Local Windows

```bash
./gradlew test :desktop:runHeadlessSmoke --no-daemon
./gradlew :desktop:packageMsi :desktop:packageExe -PrequireProductVersionPrefix=0.3.0 --no-daemon
```

## Automated CI smoke (Windows runner)

Script: `scripts/ci/windows-packaged-smoke.ps1`

| Check | Evidence |
|-------|----------|
| MSI install + file layout | CI |
| `--diagnose`, `--headless-smoke`, `--gui-smoke` | CI |
| Normal GUI alive 15s + Compose launch log | CI |
| jpackage `1.3.0` when Gradle `0.3.0` | Run **40112573212** @ `dd07d22` (RC); **re-run on final release commit** before publish |

## Owner manual acceptance (required for publish)

| # | Scenario | Outcome | Evidence |
|---|----------|---------|----------|
| 1 | Install MSI, launch packaged app | **NOT TESTED** | |
| 2 | Restart; Settings theme persists | **NOT TESTED** | |
| 3 | Connections scroll, password, reconnect | **NOT TESTED** | |
| 4 | Explorer hierarchy + PK/FK/index | **NOT TESTED** | |
| 5 | Grid paging, focus, selection, clipboard | **NOT TESTED** | |
| 6 | Dataset Builder DnD, joins, preview, save/reopen | **NOT TESTED** | |
| 7 | SQL SELECT, write rejected, cancel | **NOT TESTED** | |
| 8 | Projects links, reorder, unsaved, open | **NOT TESTED** | |
| 9 | Clean exit | **NOT TESTED** | |

Update this table after testing the **release-tag** MSI (not an older TEST artifact).

## Linux agent limits

```bash
./gradlew test :desktop:runHeadlessSmoke :desktop:packageDeb
```

Does **not** replace Windows owner acceptance.
