# ADR 001: Offline-first with local authority

## Status

Accepted (2026-10-23)

## Context

Users work on sensitive datasets with intermittent connectivity. Cloud-first ETL tools fail the offline requirement.

## Decision

The desktop client owns authoritative state for projects and pipelines. PostgreSQL runs locally (customer-managed or bundled in a later milestone). Optional sync is additive and never required.

## Consequences

- Conflict resolution deferred until sync milestone.
- Backup/restore documentation must emphasize PostgreSQL dumps.
- All features must be demonstrable in airplane mode.
