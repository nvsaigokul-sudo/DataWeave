# Test strategy

## Levels

1. **Unit / service** — validation rules (`ProjectServiceTest`).
2. **Integration** — Flyway + JDBC against embedded PostgreSQL (`ProjectRepositoryTest`).
3. **Headless smoke** — `./gradlew runHeadlessSmoke` exercises bootstrap without GUI.
4. **Manual GUI** — Windows checklist: launch with `--dev`, create project, relaunch persistence.
5. **Packaging** — MSI install/uninstall on clean VM (future).

## Commands

```bash
./gradlew test
./gradlew runHeadlessSmoke
./gradlew run --args="--dev"   # interactive shell
```

## CI (future)

- Linux: unit + integration + headless smoke.
- Windows: above + Compose run snapshot.

## Traceability

Map FR IDs in `docs/traceability-matrix.md` to test classes.
