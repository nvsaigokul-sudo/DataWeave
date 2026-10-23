# Security

## Principles

- Secrets never committed to git (see `.gitignore` for local config).
- Production connection strings stored in OS credential store (planned); v0.1 uses environment variables or `--dev` embedded DB.
- PostgreSQL role should be least privilege: `CREATE`/`SELECT`/`INSERT`/`UPDATE`/`DELETE` on application schema only — no superuser.

## Data handling

- Project metadata in PostgreSQL; source files referenced by path with future integrity hashes.
- Optional sync (future) uses TLS 1.2+ and token auth; offline mode disables sync endpoints.

## Dependency hygiene

- Gradle lock verification in CI (future).
- JDBC drivers vetted before bundling.

## Reporting

Security issues: private channel TBD with repository owner.
