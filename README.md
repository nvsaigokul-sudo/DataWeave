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
| **Windows** | **[DataWeave-Setup.msi](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/DataWeave-Setup.msi)** (Git LFS; SHA256 in [downloads/SHA256SUMS.txt](downloads/SHA256SUMS.txt)) — CI-tested build with `java.desktop` + GUI smoke. Alternate: [v0.2.1 release `.msi`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/windows-DataWeave-1.2.1.msi) / [`.exe`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/windows-DataWeave-1.2.1.exe) |
| **Linux (x64)** | [`.deb`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/linux-dataweave_1.2.1_amd64.deb) (AppImage when present on the [release page](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.1)) |
| **macOS Apple Silicon** | [`.dmg`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/macos-arm64-DataWeave-1.2.1.dmg) · [`.pkg`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/macos-arm64-DataWeave-1.2.1.pkg) |
| **macOS Intel** | Build on an Intel Mac: `./gradlew :desktop:packageDmg :desktop:packagePkg` (GitHub `macos-13` job omitted due to runner queue) |

Verify Windows with [downloads/SHA256SUMS.txt](downloads/SHA256SUMS.txt); Linux/macOS with [`SHA256SUMS.txt` on v0.2.1](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/SHA256SUMS.txt). Details: [downloads/README.md](downloads/README.md).

| Step | Action |
|------|--------|
| 1 | Open **[Releases → Latest](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** |
| 2 | Download the installer for your operating system |
| 3 | Install and launch **DataWeave** like any desktop app |
| 4 | In **Connections**, add PostgreSQL host, port, database, and credentials |

Full instructions: **[docs/INSTALL.md](docs/INSTALL.md)** · Troubleshooting: [docs/PLATFORMS.md](docs/PLATFORMS.md)

Supported: **Windows 10/11**, **Linux (x64)**, **macOS 12+** (Apple Silicon installers on Releases; Intel via local build). Installer file versions show **1.2.1** (jpackage mapping from app **0.2.1**). Each bundle includes a **private Java runtime** — no separate JDK install.

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

Documentation: [docs/VERIFICATION_STATUS.md](docs/VERIFICATION_STATUS.md) · [docs/AUDIT.md](docs/AUDIT.md) · [docs/GIT_IDENTITY.md](docs/GIT_IDENTITY.md) · [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) · [docs/PLATFORMS.md](docs/PLATFORMS.md) · [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

---

## License / signing

Installers distributed via GitHub Releases include SHA256 checksums. **Code signing and Apple notarization** are documented per release; default open builds are unsigned unless noted.
