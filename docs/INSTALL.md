# Install DataWeave (end users)

DataWeave is a **desktop application**. You do **not** need Java, Gradle, Git, or Cursor installed.

## Download

1. Open **[GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases)**.
2. Download the installer for your operating system.
3. Verify the file against `SHA256SUMS.txt` on the release page when provided.

| OS | Typical files |
|----|----------------|
| Windows | `.msi` (installer), `.exe` (app package) |
| Linux | `.AppImage` (universal), `.deb` (Debian/Ubuntu) |
| macOS | `.dmg` or `.pkg` (Apple Silicon builds from `macos-14`; Intel from `macos-13` CI runners) |

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

**“Failed to launch JVM” immediately after install** — Older installers omitted the `jdk.accessibility` module from the bundled JRE. When Windows has Java Access Bridge enabled (`%USERPROFILE%\.accessibility.properties`), AWT cannot load assistive technology classes and the launcher shows this generic error. Download the latest [DataWeave-Setup.msi](../downloads/DataWeave-Setup.msi) from `main` (see [downloads/README.md](../downloads/README.md) for SHA256) or a current GitHub Release build that includes `jdk.accessibility` in `runtime/release`.
