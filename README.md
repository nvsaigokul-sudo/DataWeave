# DataWeave

[![CI](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/ci.yml/badge.svg)](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/ci.yml)
[![Release](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/release.yml/badge.svg)](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/release.yml)

**DataWeave** is a standalone, installable **desktop application** for exploring and working with **PostgreSQL** databases — offline-first, with no mandatory cloud account or hosted backend.

---

## Download DataWeave (end users)

**You do not need to clone this repository, install Java, or run Gradle.**

**Latest release:** [github.com/nvsaigokul-sudo/DataWeave/releases/latest](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)

| Your OS | Download |
|---------|----------|
| **Windows** | **[DataWeave-Setup.msi](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/DataWeave-Setup.msi)** (Git LFS in [downloads/](downloads/README.md); SHA256 in [downloads/SHA256SUMS.txt](downloads/SHA256SUMS.txt)) |
| **Linux (x64)** | `DataWeave-*.AppImage` or `dataweave_*_amd64.deb` |
| **macOS Apple Silicon** | `.dmg` or `.pkg` from the **macos-arm64** build |
| **macOS Intel** | `.dmg` or `.pkg` from the **macos-x64** build |

Verify Windows MSI with [downloads/SHA256SUMS.txt](downloads/SHA256SUMS.txt); other platforms use **`SHA256SUMS.txt`** on the release page. Hub: [downloads/README.md](downloads/README.md).

| Step | Action |
|------|--------|
| 1 | Open **[Releases → Latest](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** |
| 2 | Download the installer for your operating system |
| 3 | Install and launch **DataWeave** like any desktop app |
| 4 | In **Connections**, add PostgreSQL host, port, database, and credentials |

Full instructions: **[docs/INSTALL.md](docs/INSTALL.md)** · Troubleshooting: [docs/PLATFORMS.md](docs/PLATFORMS.md)

Supported: **Windows 10/11**, **Linux (x64)**, **macOS 12+** (separate Apple Silicon and Intel installers). Each bundle includes a **private Java runtime** — no separate JDK install.

---

## What the application does

- **Compose Desktop UI** + **embedded Spring Boot** (single process — no separate server to start)
- Save **real PostgreSQL** connections (host, port, SSL, credentials in OS keychain/credential manager)
- Browse schemas/tables and view **paginated** table data from your database
- Local **SQLite** metadata at `~/.dataweave/` (not your business data warehouse)

No fake demo databases, Docker, or browser UI.

---

## For developers (source build)

Requirements: **JDK 21**, Git.

```bash
git clone https://github.com/nvsaigokul-sudo/DataWeave.git
cd DataWeave
./gradlew test :desktop:runHeadlessSmoke
./gradlew :desktop:run
```

Optional dev embedded PostgreSQL: `./gradlew :desktop:run --args="--dev"` (development only).

Package for current OS:

```bash
./gradlew :desktop:packageDistributionForCurrentOS
```

Windows (on a Windows host or CI): `:desktop:packageMsi` and `:desktop:packageExe`. Linux: `:desktop:packageDeb`, `:desktop:packageAppImage`. macOS: `:desktop:packageDmg`, `:desktop:packagePkg`.

Artifacts under `desktop/build/compose/binaries/`.

Release automation: [.github/workflows/release.yml](.github/workflows/release.yml) (tag `v*`).

Documentation: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) · [docs/PLATFORMS.md](docs/PLATFORMS.md) · [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

---

## License / signing

Installers distributed via GitHub Releases include SHA256 checksums. **Code signing and Apple notarization** are documented per release; default open builds are unsigned unless noted.
