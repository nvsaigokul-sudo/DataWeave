# Windows release acceptance (v0.3.0 RC)

**Not a published release.** Use this checklist after building installers from commit **`3126f14`** or later on branch **`cursor/dataweave-product-v0.3.0-d9c8`**.

## Build MSI/EXE (Windows or GitHub Actions)

### Option A — GitHub Actions (recommended)

1. Push to `cursor/dataweave-product-v0.3.0-d9c8`, or run workflow **Windows test installer (no release)** manually (`workflow_dispatch`).
2. Wait for job **Windows MSI test artifact** on `windows-latest`.
3. Download artifact **`TEST-windows-msi-<sha>`** (contains `.msi` and `.exe`).
4. Record the workflow step **Report version and SHA256** values (authoritative for that build).

Workflow file: `.github/workflows/windows-test-installer.yml`  
Gradle tasks: `:desktop:packageMsi` `:desktop:packageExe` (see `desktop/build.gradle.kts`).

Automated install/GUI smoke on CI: `scripts/ci/windows-packaged-smoke.ps1` (15s alive + Compose launch log).

### Option B — Local Windows machine

```bash
./gradlew test :desktop:runHeadlessSmoke --no-daemon
./gradlew :desktop:packageMsi :desktop:packageExe --no-daemon
```

Artifacts under: `desktop/build/compose/binaries/main/msi/` and `.../exe/`.

Compute checksum:

```powershell
Get-FileHash -Algorithm SHA256 (Get-ChildItem -Recurse desktop/build/compose/binaries -Filter *.msi | Select-Object -First 1).FullName
```

## Owner manual acceptance (record PASS / PARTIAL / FAIL / NOT TESTED)

| # | Scenario | Notes |
|---|----------|--------|
| 1 | Install MSI, launch packaged app | No HeadlessException |
| 2 | Restart app; Settings Light/Dark/System persist | |
| 3 | Connections: scroll, save password, reconnect after restart | Credential recovery if vault empty |
| 4 | Explorer: DB → schema → table; PK/FK/index sections; Retry on error | |
| 5 | Data grid: paging, filter, row/col selection, Ctrl+C / Ctrl+Shift+C, NULL vs empty | Keyboard focus |
| 6 | Dataset Builder: drag tables, join lines, key pickers, preview, invalid graph keeps last preview, save/reopen | |
| 7 | SQL: SELECT ok; write statements rejected; Cancel long query | |
| 8 | Projects: link dataset/query, reorder, unsaved dialog, Open in Datasets / Open in SQL | |
| 9 | Clean exit; no hung PG pools | |

Do **not** publish `v0.3.0` or create a GitHub Release until this checklist is complete and you explicitly authorize publish.

## Linux cloud agent limitations

Linux CI/agents **cannot** produce Windows MSI or perform interactive GUI acceptance. Linux verification is limited to:

```bash
./gradlew test :desktop:runHeadlessSmoke :desktop:packageDeb
```

That does **not** satisfy Windows release gates.
