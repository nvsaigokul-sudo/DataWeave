# Risks and backlog

## Risks

| ID | Risk | Impact | Mitigation |
|----|------|--------|------------|
| R1 | Customer PostgreSQL not installed on Windows | High | Document install; spike bundled PG in M3 |
| R2 | Compose Desktop Windows packaging gaps | Medium | CI Windows runner for MSI smoke |
| R3 | Preview grid performance overstated | Medium | Measure before marketing; use budgets doc |
| R4 | JDBC driver licensing | Medium | Curated allow-list, optional packs |
| R5 | Embedded PG version skew in tests | Low | Migration tests on customer PG version matrix |

## Backlog (prioritized)

1. CSV source registration + sample job
2. Virtualized preview grid + instrumentation
3. Pipeline graph editor
4. Local execution engine with cancel/resume
5. Connection vault in OS credential store
6. Project delete with confirmation
7. Diagnostic bundle export
8. Optional sync bundle format
