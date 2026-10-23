# Development

## Prerequisites

- JDK 21+
- For production-like runs: PostgreSQL 14+ and a database role for DataWeave

## Quick start (embedded PostgreSQL)

```bash
./gradlew run --args="--dev"
```

## External PostgreSQL

```bash
export DATAWEAVE_JDBC_URL=jdbc:postgresql://127.0.0.1:5432/dataweave
export DATAWEAVE_DB_USER=dataweave
export DATAWEAVE_DB_PASSWORD=yourpassword
./gradlew run
```

See `config/application.example.properties`.

## Tests

```bash
./gradlew test runHeadlessSmoke
```

## Project structure

See `docs/ARCHITECTURE.md`.

## Conventions

- Flyway migrations in `app/src/main/resources/db/migration`
- Conventional commits: `feat:`, `docs:`, `test:`, `chore:`
