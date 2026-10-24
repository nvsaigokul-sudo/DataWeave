# Install DataWeave (end users)

DataWeave is a **desktop application**. You do **not** need Java, Gradle, Git, or Cursor installed.

## Download

1. Open **[GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases)** (or for Windows, the CI-tested LFS MSI linked from the [README](../README.md#download-dataweave-end-users)).
2. Download the installer for your operating system.
3. Verify checksums: Windows LFS MSI → [downloads/SHA256SUMS.txt](../downloads/SHA256SUMS.txt); release bundles → `SHA256SUMS.txt` on the release page.

   **Windows (mandatory before troubleshooting):** confirm the MSI you installed matches the hash in [downloads/windows/SHA256SUMS.txt](../downloads/windows/SHA256SUMS.txt) (`35333f603bac…`). Legacy installers (`a9c8ab77…`, `b613ec64…`) and the v0.2.1 **release** MSI (`6716b5ae…`) differ from this file. Older LFS builds have **no** `app\resources\DataWeave-JvmDebug.cmd` and use `DataWeaveAppKt` — uninstall (**Settings → Apps → DataWeave → Uninstall**), delete stale Start Menu shortcuts, download via the README **media.githubusercontent.com** link (not `raw.githubusercontent.com`, which is an LFS pointer), then reinstall.

| OS | Typical files |
|----|----------------|
| Windows | `.msi` (installer), `.exe` (app package) |
| Linux | `.AppImage` (universal), `.deb` (Debian/Ubuntu) |
| macOS | `.dmg` or `.pkg` (Apple Silicon from [Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases); Intel: local `./gradlew :desktop:packageDmg`) |

## First launch

1. Install or run the downloaded package.
2. Open **Connections** and enter your **PostgreSQL** host, port, database, username, password, and SSL mode.
3. Click **Test & browse** — DataWeave connects to **your** database (no demo/fake data).
4. Use **Browse data** to list schemas, tables, and paginated rows.

Launch diagnostics and smoke logs (when using `--diagnose`, `--gui-smoke`, or startup failures):

- Windows: `%LOCALAPPDATA%\DataWeave\` (`launch.log`, `launch-error.log`, `jvm-debug.log` from JvmDebug.cmd)
- Linux/macOS: `~/.dataweave/logs/` (`launch.log`, `launch-error.log`)

Local app data (saved connection names, workspace projects) is stored in:

- `~/.dataweave/metadata.db` (Linux/macOS)
- `%USERPROFILE%\.dataweave\metadata.db` (Windows)

Passwords are stored in the OS credential vault when available.

## Prerequisites

- **PostgreSQL** server reachable from your machine (local or remote). The app launches without PostgreSQL; you add a connection when ready.
- Network only for remote PostgreSQL or optional update checks — no mandatory cloud account.

## Offline use

Workspace projects and saved connection profiles work offline. Querying remote PostgreSQL requires network access to that server.

## Security note

Release binaries are **not code-signed/notarized** unless stated on a specific release. Download only from the official GitHub Releases page.

## Troubleshooting (Windows)

**“Failed to launch JVM”** — The native launcher shows this message for both a broken bundled JRE and for **uncaught Java/Compose startup errors** (Skiko/AWT/Spring). Collect diagnostics before reinstalling:

1. From PowerShell (repo script or copy from `scripts/diagnostics/DataWeave-Windows-Diagnostics.ps1`):

   ```powershell
   .\scripts\diagnostics\DataWeave-Windows-Diagnostics.ps1 -RunPackagedDiagnose
   ```

2. Or from an installed copy:

   ```powershell
   & "C:\Program Files\DataWeave\DataWeave.exe" --diagnose
   ```

3. If **step 2 also shows “Failed to launch JVM”** (failure before Java `main`), bypass the native launcher:

   ```cmd
   "C:\Program Files\DataWeave\app\resources\DataWeave-JvmDebug.cmd" --diagnose
   ```

   Output is appended to `%LOCALAPPDATA%\DataWeave\jvm-debug.log` (includes a full `--diagnose` block when the bundled runtime is intact).

4. Check `%LOCALAPPDATA%\DataWeave\launch-error.log` after a failed start (Java-side failures after `main`; recent builds).

5. **Prove you have a bootstrap build:** open `C:\Program Files\DataWeave\app\DataWeave.cfg` and confirm a line contains `app.mainclass=com.dataweave.app.DataWeaveBootstrapKt`. If it says `DataWeaveAppKt`, you still have a pre-bootstrap MSI — reinstall from [downloads/windows/DataWeave-Setup.msi](../downloads/windows/DataWeave-Setup.msi) after verifying SHA256.

**Bundled `java.exe`:** current MSIs ship `C:\Program Files\DataWeave\runtime\bin\java.exe` (used by `DataWeave-JvmDebug.cmd`). jpackage’s default runtime strips native commands; DataWeave keeps `java.exe` in the bundled runtime for the debug launcher. The normal **`DataWeave.exe`** launcher does not require `java.exe` on `PATH`.

**Older installers** (before `ce8e76c`) may omit **`java.desktop`** and/or **`jdk.accessibility`** from the bundled JRE. Headless smoke can pass while **GUI launch fails** with the same generic dialog. Check `runtime\release` on your install: `MODULES=` must include both `java.desktop` and `jdk.accessibility`. Download the latest [DataWeave-Setup.msi](../downloads/windows/DataWeave-Setup.msi) (see [downloads/README.md](../downloads/README.md) for SHA256) and remove stale Start Menu shortcuts pointing at an old install path.
