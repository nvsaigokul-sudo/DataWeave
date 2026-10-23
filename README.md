# DataWeave

**DataWeave** is a standalone, installable **desktop application** for exploring and working with **PostgreSQL** databases — offline-first, with no mandatory cloud account or hosted backend.

---

## Download and install (end users)

**You do not need to clone this repository or run Gradle.**

| Step | Action |
|------|--------|
| 1 | Go to **[GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases)** |
| 2 | Download the installer for Windows, Linux, or macOS |
| 3 | Install and launch **DataWeave** like any desktop app |
| 4 | Add your PostgreSQL connection in the **Connections** tab and browse your data |

Full instructions: **[docs/INSTALL.md](docs/INSTALL.md)**

Supported: **Windows 10/11**, **Linux (x64)**, **macOS** (Apple Silicon and Intel builds via CI). Each installer bundles a **private Java runtime** — you do not install JDK separately.

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

Artifacts under `desktop/build/compose/binaries/`.

Release automation: [.github/workflows/release.yml](.github/workflows/release.yml) (tag `v*`).

Documentation: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) · [docs/PLATFORMS.md](docs/PLATFORMS.md) · [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

---

## License / signing

Installers distributed via GitHub Releases include SHA256 checksums. **Code signing and Apple notarization** are documented per release; default open builds are unsigned unless noted.
