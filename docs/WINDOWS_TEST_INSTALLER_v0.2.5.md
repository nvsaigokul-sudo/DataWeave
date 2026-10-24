# Windows test installer — v0.2.5 (UNPUBLISHED)

Use this for **laptop testing only**. This is **not** a GitHub Release and does **not** replace [v0.2.4](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.4) assets.

| Field | Value |
|-------|-------|
| Branch | `cursor/release-v0.2.5-prep-d9c8` |
| Gradle version | **0.2.5** |
| jpackage / MSI file version | **1.2.5** (`DataWeave-1.2.5.msi`) |
| Scroll fix | commit `3c073f8` |
| v0.2.4 MSI SHA256 (do not confuse) | `db19e0c9…` — see [downloads/SHA256SUMS.txt](../downloads/SHA256SUMS.txt) |

## Download from GitHub Actions (recommended)

1. Open the workflow run: [Windows test installer (no release) — run 40076910387](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40076910387).
2. Under **Artifacts**, download **`TEST-windows-msi-184db3f`** (name includes commit SHA).
3. Unzip and install **`DataWeave-1.2.5.msi`** only (ignore any other MSI names if present in older artifacts).
4. Verify SHA256 before install:

```powershell
Get-FileHash -Algorithm SHA256 .\DataWeave-1.2.5.msi
# Expected: 52cec955bac4ac21e9160e10845b678af098aa5951a0dbb91833c239e3857b21
```

**Re-run builds:** Actions → **Windows test installer (no release)** → **Run workflow** → branch **`cursor/release-v0.2.5-prep-d9c8`**. Each run prints MSI name + SHA256 in the job summary.

Pushing to `cursor/release-v0.2.5-prep-d9c8` also triggers this workflow. It does **not** run `release.yml` or publish tags.

## Install on Windows 11

1. Uninstall any older DataWeave (**Settings → Apps**).
2. Double-click **`DataWeave-1.2.5.msi`** (or `msiexec /i DataWeave-1.2.5.msi`).
3. Launch from Start Menu **DataWeave**.
4. Confirm version in **Settings → Apps → DataWeave** shows **1.2.5** (jpackage product version).

Optional diagnostics:

```powershell
& "$env:LOCALAPPDATA\Programs\DataWeave\DataWeave.exe" --diagnose
# Expect: java.awt.headless=false after normal GUI path (v0.2.3+ fix)
```

## Build locally (PowerShell, repo root)

Requires **JDK 21** (Temurin or Microsoft Build of OpenJDK). No separate JRE for dev runs.

```powershell
git clone https://github.com/nvsaigokul-sudo/DataWeave.git
cd DataWeave
git checkout cursor/release-v0.2.5-prep-d9c8
.\gradlew.bat test :desktop:runHeadlessSmoke --no-daemon
.\gradlew.bat :desktop:packageMsi --no-daemon
```

MSI output:

```text
desktop\build\compose\binaries\main\msi\DataWeave-1.2.5.msi
```

Run from source (no MSI):

```powershell
.\gradlew.bat :desktop:run --no-daemon
```

## Connections page — manual checklist (scroll fix)

Cloud agents **cannot** verify Windows GUI scroll. On your laptop after installing **1.2.5**:

| Step | Pass? |
|------|-------|
| Open **Connections** from the workspace nav | |
| Resize window shorter than full screen (or use 100% display scaling) | |
| Fill host/port/database/user/password so the form is taller than the pane | |
| **Mouse wheel** scrolls the form; saved profiles below remain reachable | |
| No “stuck” middle section; single scroll bar for the whole Connections content | |
| **Test & browse** still works with a real PostgreSQL instance | |
| Switch to **Browse data** and back to **Connections** — scroll position reasonable | |

Report failures with `%LOCALAPPDATA%\DataWeave\launch-error.log` and display scaling (e.g. 125%).

## What we did not do

- No **`v0.2.5`** tag push
- No merge to **`main`** (unless you open/merge the prep PR separately)
- No overwrite of **v0.2.4** release or LFS MSI
