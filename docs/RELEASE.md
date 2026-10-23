# Release process (draft)

## Versioning

SemVer starting at `0.1.0-SNAPSHOT` until first Windows MSI.

## Pre-release checklist

- [ ] `./gradlew test runHeadlessSmoke` green
- [ ] Flyway migration applied on clean PostgreSQL
- [ ] Traceability matrix updated
- [ ] KNOWN_LIMITATIONS.md reviewed
- [ ] No secrets in working tree
- [ ] Windows MSI smoke (when agent available)

## Build MSI (local)

```bash
./gradlew packageMsi
```

Artifact under `app/build/compose/binaries`.

## Rollback

Restore previous MSI and run Flyway repair only under operator guidance — forward-only migrations preferred.
