# Windows 11 verification checklist

**Environment note:** Cloud agent runs on **Linux**; items marked *Linux verified* were run there. **Windows UI/packaging require your machine.**

## Prerequisites

- JDK 21 (e.g. Microsoft Build of OpenJDK or Temurin)
- Optional: local PostgreSQL 14+ (or use `--dev` embedded PostgreSQL for demos only)

## Commands (repository root)

```powershell
.\gradlew.bat --version
.\gradlew.bat clean test
.\gradlew.bat runHeadlessSmoke
.\gradlew.bat run --args="--dev"
```

Optional packaging:

```powershell
.\gradlew.bat :desktop:packageMsi
```

## Verify

| Area | Action | Evidence to capture |
|------|--------|---------------------|
| Build | `clean test` | Gradle `BUILD SUCCESSFUL`, test count |
| Backend | `runHeadlessSmoke` | Line `HEADLESS_SMOKE_OK` |
| GUI | `run --args="--dev"` | Compose window, create connection, browse table pages |
| PostgreSQL | Save connection to local instance | Test & browse succeeds |
| Credentials | Save connection with password | Password not in `%USERPROFILE%\.dataweave\metadata.db` plaintext; Credential Manager entry on Windows |
| Cache | Open same table page twice | Cache diagnostics hit count increases (future UI surfacing) |
| Packaging | `packageMsi` | MSI installs and launches on clean profile |
| Network | While app running | `netstat -ano` shows **no** Spring HTTP listener |

## Linux agent results (2026-10-23)

- `./gradlew test` — **PASS** (backend integration tests)
- `./gradlew runHeadlessSmoke` — **PASS** (`HEADLESS_SMOKE_OK`, Spring started ~1.1s in sample run)
- Compose GUI — **UNVERIFIED** (GL context unavailable in cloud VM)
