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

On **windows-latest**, CI also silent-installs the built MSI and runs packaged `DataWeave.exe --diagnose`, `--headless-smoke`, `--gui-smoke`, and a **normal GUI launch** (no args, process alive 15s) from the install root, a non-install CWD, and the Start Menu shortcut working directory when present. Access Bridge is enabled in the job user profile to catch missing `jdk.accessibility`. CI does **not** pixel-test Compose UI or exercise PostgreSQL browse on Windows; those remain manual ([WINDOWS_VERIFICATION.md](./WINDOWS_VERIFICATION.md)).

## Traceability

Map FR IDs in `docs/traceability-matrix.md` to test classes.
