# ADR 002: PostgreSQL as metadata system of record

## Status

Accepted (2026-10-23)

## Context

SQLite simplifies embedding but weakens alignment with enterprise JDBC tooling and future multi-process extensions. PostgreSQL matches customer infra skills and supports rich JSON columns for pipeline graphs.

## Decision

Use PostgreSQL 14+ via JDBC, HikariCP pooling, and Flyway migrations. Bulk transform data stays in files/streams, not bytea tables.

## Consequences

- Windows installs must document PostgreSQL installation or bundle (TBD M3).
- Tests use embedded PostgreSQL binaries (zonky) — version may differ from production; migration compatibility tests remain mandatory.
