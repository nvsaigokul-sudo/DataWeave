# Test strategy

## Levels

1. **Unit / service** — validation rules (`WorkspaceProjectServiceTest`, `PlatformInfoTest`).
2. **Integration** — Spring Boot + Flyway (SQLite) + embedded PostgreSQL (`ConnectionAndCacheIntegrationTest`).
3. **Headless smoke** — `./gradlew :desktop:runHeadlessSmoke` (connections + schema browse path).
4. **Manual GUI** — [CROSS_PLATFORM_VERIFICATION.md](./CROSS_PLATFORM_VERIFICATION.md) per OS.
5. **Packaging** — Native package task on each OS (AppImage / MSI / DMG).

## Commands

```bash
./gradlew test
./gradlew :desktop:runHeadlessSmoke
./gradlew :desktop:run --args="--dev"
```

## CI

GitHub Actions matrix (`.github/workflows/ci.yml`): **ubuntu-latest**, **windows-latest**, **macos-14** — `test`, `runHeadlessSmoke`, OS-specific package task.

## Traceability

Map FR IDs in `docs/traceability-matrix.md` to test classes.
