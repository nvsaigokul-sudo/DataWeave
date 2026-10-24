# Install DataWeave (end users)

DataWeave is a **desktop application**. You do **not** need Java, Gradle, Git, or Cursor installed.

## Download

1. Open **[GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases)** (or for Windows, the CI-tested LFS MSI linked from the [README](../README.md#download-dataweave-end-users)).
2. Download the installer for your operating system.
3. Verify checksums: Windows LFS MSI → [downloads/SHA256SUMS.txt](../downloads/SHA256SUMS.txt); release bundles → `SHA256SUMS.txt` on the release page.

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

3. Check `%LOCALAPPDATA%\DataWeave\launch-error.log` after a failed start (written by recent builds).

**Older installers** (before `ce8e76c`) may omit **`java.desktop`** and/or **`jdk.accessibility`** from the bundled JRE. Headless smoke can pass while **GUI launch fails** with the same generic dialog. Check `runtime\release` on your install: `MODULES=` must include both `java.desktop` and `jdk.accessibility`. Download the latest [DataWeave-Setup.msi](../downloads/DataWeave-Setup.msi) (see [downloads/README.md](../downloads/README.md) for SHA256) and remove stale Start Menu shortcuts pointing at an old install path.
