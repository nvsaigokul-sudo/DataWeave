# DataWeave

Offline-first Windows desktop workbench for local data integration. PostgreSQL holds project and pipeline metadata; transforms run on the desktop without requiring cloud connectivity.

## Session 1 deliverable

- Product documentation pack under `docs/` (PRD with 41 sections, architecture, ADRs, test/release/security docs)
- Kotlin + Compose Desktop application shell
- Vertical slice: **create and list projects** persisted via Flyway + PostgreSQL

## Run locally

```bash
./gradlew run --args="--dev"
```

Or point at an existing database:

```bash
export DATAWEAVE_JDBC_URL=jdbc:postgresql://127.0.0.1:5432/dataweave
export DATAWEAVE_DB_USER=dataweave
export DATAWEAVE_DB_PASSWORD=
./gradlew run
```

## Test

```bash
./gradlew test runHeadlessSmoke
```

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
