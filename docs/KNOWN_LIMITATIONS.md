# Known limitations (v0.1)

- **Windows-first packaging** — dev verified on Linux; MSI not yet produced in Session 1 CI.
- **PostgreSQL required** — use `--dev` embedded mode for local demos; production expects customer PostgreSQL.
- **Navigation placeholders** — Sources, Pipelines, Settings are disabled in the shell.
- **No delete UI** — repository supports delete; UI omits until confirmation design lands.
- **No performance SLAs** — budgets awaiting measurement (see `docs/performance-budget.md`).
- **Embedded test PostgreSQL 14.x** — may differ from customer server version; migrations must stay compatible.
