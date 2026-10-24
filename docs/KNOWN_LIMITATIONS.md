# Known limitations

Last reviewed: **2026-10-24** (`main` engineering reset).

## Packaging & platforms

- **Release v0.2.1 vs LFS Windows MSI** — different build runs (different SHA256). Prefer [downloads/windows/DataWeave-Setup.msi](../downloads/windows/DataWeave-Setup.msi) for the CI GUI-tested build.
- **Code signing / notarization** — not applied; see [INSTALL.md](./INSTALL.md).
- **macOS Intel** — not built on GitHub Actions; build locally on an Intel Mac.
- **End-user Windows/macOS laptops** — not verified in cloud agents; see [VERIFICATION_STATUS.md](./VERIFICATION_STATUS.md).

## Product scope

- **PostgreSQL required for data** — local SQLite stores app metadata only; remote PG must be reachable to browse tables.
- **Navigation placeholders** — Sources, Pipelines, Settings remain disabled in the shell until later milestones.
- **No delete UI** — backend supports delete; UI omits until confirmation design lands.
- **Dataset builder / pipelines / sync** — not implemented (see [IMPLEMENTATION_ROADMAP.md](./IMPLEMENTATION_ROADMAP.md)).
- **Large grid / 20M-row performance** — page-sized fetch and `LazyColumn` only; no production-scale perf proof.

## Security

- **Windows credential CLI** — `-w` on command line can expose secrets in process lists; native Keychain API deferred.

## Testing

- **Embedded PostgreSQL 14.x** in tests may differ from customer server versions; SQL must stay portable.
