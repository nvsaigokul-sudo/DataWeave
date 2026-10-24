# DataWeave

[![CI](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/ci.yml/badge.svg)](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/ci.yml)
[![Release](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/release.yml/badge.svg)](https://github.com/nvsaigokul-sudo/DataWeave/actions/workflows/release.yml)

**DataWeave** is a standalone, installable **desktop application** for exploring and working with **PostgreSQL** databases — offline-first, with no mandatory cloud account or hosted backend.

---

## Download DataWeave (end users)

**You do not need to clone this repository, install Java, or run Gradle.**

**Latest published release:** [v0.2.4](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.4) · **v0.2.5** (Connections scroll fix) is **UNPUBLISHED** — prep on `cursor/release-v0.2.5-prep-d9c8`; see [docs/V0.2.5_RELEASE.md](docs/V0.2.5_RELEASE.md).

| Your OS | Download (**published v0.2.4** — use until v0.2.5 is approved) |
|---------|----------|
| **Windows** | **[Releases → v0.2.4 `.msi`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.4/windows-DataWeave-Setup.msi)** (SHA256 `db19e0c9…`) — PG connection sessions + workspace UI. [LFS/media MSI](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi) matches after `main` refresh. [SHA256SUMS](downloads/SHA256SUMS.txt) |
| **Linux (x64)** | [`.deb` (v0.2.4)](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.4/linux-dataweave_1.2.4_amd64.deb) · [linux/](downloads/linux/) |
| **macOS Apple Silicon** | [`.dmg` / `.pkg` (v0.2.4)](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.4) · [macos/](downloads/macos/) |
| **macOS Intel** | Local build — [downloads/macos/README.md](downloads/macos/README.md) (`macos-13` CI omitted) |

Verify with `./scripts/verify-downloads-checksums.sh` and per-platform [downloads/SHA256SUMS.txt](downloads/SHA256SUMS.txt); release bundles use [`SHA256SUMS.txt` on v0.2.1](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/SHA256SUMS.txt). Details: [downloads/README.md](downloads/README.md).

| Step | Action |
|------|--------|
| 1 | Open **[Releases → Latest](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** |
| 2 | Download the installer for your operating system |
| 3 | Install and launch **DataWeave** like any desktop app |
| 4 | In **Connections**, add PostgreSQL host, port, database, and credentials |

Full instructions: **[docs/INSTALL.md](docs/INSTALL.md)** · Troubleshooting: [docs/PLATFORMS.md](docs/PLATFORMS.md)

Supported: **Windows 10/11**, **Linux (x64)**, **macOS 12+** (Apple Silicon installers on Releases; Intel via local build). Published installers use file version **1.2.4** (jpackage mapping from app **0.2.4**); prep **0.2.5** → **1.2.5** after publish. Each bundle includes a **private Java runtime** — no separate JDK install.

---

## What the application does

- **Compose Desktop UI** + **embedded Spring Boot** (single process — no separate server to start)
- Save **real PostgreSQL** connections (host, port, SSL, credentials in OS keychain/credential manager)
- Browse schemas/tables and view **paginated** table data from your database
- **SQL editor** (read-only `SELECT`) with local query history
- **Dataset builder** — column preview, CSV file registration/preview, inner-join preview; **Export CSV** from table browse (streamed; optional limit)
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

Documentation: [docs/COMPLETION_TRACKER.md](docs/COMPLETION_TRACKER.md) · [docs/VERIFICATION_STATUS.md](docs/VERIFICATION_STATUS.md) · [docs/AUDIT.md](docs/AUDIT.md) · [docs/GIT_IDENTITY.md](docs/GIT_IDENTITY.md) · [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) · [docs/PLATFORMS.md](docs/PLATFORMS.md) · [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

---

## License / signing

Installers distributed via GitHub Releases include SHA256 checksums. **Code signing and Apple notarization** are documented per release; default open builds are unsigned unless noted.
