# DataWeave

Offline-first Windows desktop workbench for local data integration. PostgreSQL holds project and pipeline metadata; transforms run on the desktop without requiring cloud connectivity.

## Current stack

- **Desktop:** Kotlin + Compose Desktop (`desktop/`)
- **Backend:** Java 21 + embedded Spring Boot (`backend/`, no HTTP server)
- **Local metadata:** SQLite + Flyway (`~/.dataweave/metadata.db`)
- **Data source:** user PostgreSQL connections (HikariCP + JDBC)
- **Cache:** Caffeine (metadata, data pages, previews, read-only SELECT)

## Vertical slices shipped

- Workspace projects (SQLite)
- Saved PostgreSQL connections + credential vault
- Schema/table browser with metadata cache refresh
- Paginated table grid (async fetch, stale-request guard)

## Run locally

```bash
./gradlew :desktop:run --args="--dev"
```

Configure a saved connection in the UI (Connections tab). PostgreSQL is **not** required to launch the app.

## Test

```bash
./gradlew test runHeadlessSmoke
```

Windows checklist: [docs/WINDOWS_VERIFICATION.md](docs/WINDOWS_VERIFICATION.md)

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Implementation roadmap](docs/IMPLEMENTATION_ROADMAP.md)
- [PRD (complete)](docs/prd/PRD-COMPLETE.md)
- [Development guide](docs/DEVELOPMENT.md)

## Git remotes

After clone, add GitHub:

```bash
git remote add github https://github.com/nvsaigokul-sudo/DataWeave.git
```

Cursor cloud environments may retain a separate `origin` for agent pushes.
