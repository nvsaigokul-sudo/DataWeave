# Cross-platform support

DataWeave targets **Windows 10/11**, **Linux (x64)**, and **macOS (Intel and Apple Silicon)** with one codebase: Kotlin Compose Desktop + embedded Spring Boot.

## Supported artifacts (built on the host OS)

| Host OS | Gradle packaging tasks | Formats |
|---------|------------------------|---------|
| Windows | `:desktop:packageMsi`, `:desktop:packageExe` | MSI, EXE |
| Linux | `:desktop:packageAppImage` (default target); `:desktop:packageDeb` if `fakeroot`/`dpkg` installed | AppImage |
| macOS | `:desktop:packageDmg`, `:desktop:packagePkg` | DMG, PKG |

CI (`.github/workflows/ci.yml`) runs `test`, `runHeadlessSmoke`, platform packaging, and on **windows-latest** a silent MSI install plus packaged `DataWeave.exe --diagnose`, `--headless-smoke`, `--gui-smoke`, and a **normal GUI launch** (no args, alive 15s). Access Bridge is enabled in the job profile to catch missing `jdk.accessibility`. Bundled `runtime/release` must list `java.desktop` and `jdk.accessibility`.

## Local data paths

| Data | Location |
|------|----------|
| SQLite metadata | `~/.dataweave/metadata.db` (all OS; `%USERPROFILE%\.dataweave` on Windows) |
| Logs | Console / SLF4J simple (local only) |

Override metadata DB: `--dataweave.metadata.jdbc-url=jdbc:sqlite:/path/to/metadata.db`

## Credential storage

| OS | Implementation | Source set |
|----|----------------|------------|
| Windows | Credential Manager (`CredWrite`) | `backend/src/windows/java` |
| macOS | Keychain via `/usr/bin/security` | `backend/src/macos/java` |
| Linux | freedesktop **Secret Service** via `secret-tool` when available; otherwise in-process memory for that session | `backend/src/linux/java` |
| Fallback | In-memory (warn once) if no OS vault bean is registered | `backend/src/main/java` |

Passwords are **never** stored in SQLite or git.

## Embedded PostgreSQL (`--dev`)

Uses [embedded-postgres](https://github.com/zonkyio/embedded-postgres) native binaries. Supported for development/smoke tests on common CI hosts. **Apple Silicon** uses zonky binaries when available for the host; if a binary is missing, use an external PostgreSQL instance instead of `--dev`.

## macOS GUI requirement

Compose Desktop requires `-XstartOnFirstThread` on macOS; the Gradle `desktop` module applies this to `JavaExec` tasks automatically.

## Verification status

| Platform | Automated CI | Manual GUI (agent 2026-10-23) |
|----------|--------------|-------------------------------|
| Linux x64 | Expected via GitHub Actions | **Verified** locally in cloud VM (`test`, `runHeadlessSmoke`) |
| Windows | MSI install + `--diagnose` / `--headless-smoke` / `--gui-smoke` / normal GUI alive 15s on `windows-latest` ([39996985661](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39996985661)) | **CI GUI path verified**; full AppShell UX still manual |
| macOS (ARM) | Expected via GitHub Actions | **Not verified** in agent VM |

See [CROSS_PLATFORM_VERIFICATION.md](./CROSS_PLATFORM_VERIFICATION.md) for manual checklists.
