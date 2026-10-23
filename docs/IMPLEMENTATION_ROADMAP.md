# DataWeave Implementation Roadmap

## Phase summary

| Phase | Goal | Session 1 status |
|-------|------|------------------|
| A | Assessment, architecture, ADRs, risks | Complete |
| B | PRD pack (41 sections), traceability, NFR docs | Complete |
| C | Technology ADR, scaffold, build + shell | Complete |
| D | First vertical slice + tests | Complete (projects CRUD UI + PostgreSQL) |

## Milestones

### M0 — Foundation (current)

- Gradle + Kotlin 2 + Compose Desktop shell
- Flyway migrations against PostgreSQL
- Project workspace list/create persisted locally
- Embedded PostgreSQL for `--dev` and automated tests

### M1 — Source ingest (next)

- CSV registration per project
- Sample extraction (bounded row count)
- Virtualized preview grid with performance instrumentation

### M2 — Pipeline designer

- Node graph model stored in PostgreSQL
- Validation (acyclic graph, typed ports)
- Single-threaded local execution engine

### M3 — Windows packaging

- MSI via Compose native distributions
- Bundled PostgreSQL option spike vs external service
- Code signing pipeline (customer-provided certificates)

### M4 — Optional sync

- Export/import project bundle
- Authenticated remote registry (opt-in)

## Engineering gates

Each milestone requires: migration forward test, headless smoke, Windows GUI checklist, updated traceability matrix, and performance measurements before updating budgets.
