# ADR 006: Engineering reset — repair in place (Option A)

**Status:** Accepted  
**Date:** 2026-10-24  
**Context:** Packaged Windows builds failed on some machines (`Failed to launch JVM`, missing `DataWeave-JvmDebug.cmd`, stripped `java.exe`, wrong jpackage main class). Release tag **v0.2.1** artifacts differ from CI-tested LFS MSI. Gradle version is `0.2.1-SNAPSHOT` while jpackage emits **1.2.1** (Compose/jpackage major-version rule).

## Decision

**Option A — repair the existing Compose Desktop + jpackage stack** rather than Option B (rebuild launcher/installer with a new native shell or custom installer framework).

## Rationale

| Criterion | Option A (repair) | Option B (rebuild parts) |
|-----------|-------------------|---------------------------|
| Scope | Gradle modules list, bootstrap main class, one CMD helper, CI smoke, downloads alignment | New installer, duplicate launch paths, migration for installed users |
| Risk | Low — changes are isolated and covered by CI | Medium — new packaging surface, signing, update story |
| Time to reliability | CI already proves MSI on `windows-latest` after fixes | Unknown — new integration work |
| Product | Same single-process Spring + Compose architecture (ADR 004) | Same app, different delivery layer |

Smallest path to **reliable launch**:

1. **`DataWeaveBootstrapKt`** as jpackage `mainClass` with early `launch-error.log` before AWT.
2. **Explicit jlink modules** including `java.desktop` and `jdk.accessibility`.
3. **`stripNativeCommands = false`** so `runtime/bin/java.exe` exists for **`DataWeave-JvmDebug.cmd`** under `app/resources/`.
4. **JvmDebug.cmd** resolves install root via `%~dp0..\..` (not `%CD%`).
5. **CI** silent MSI install + `--diagnose`, JvmDebug, headless/GUI smoke, normal GUI 15s.
6. **Downloads:** LFS MSI refreshed from green CI; document hash mismatch vs release v0.2.1 MSI.

Option B is deferred until repair fails on verified user hardware or signing/notarization requires a different pipeline.

## Consequences

- Users on stale MSIs must reinstall from `downloads/windows/` (SHA256 in `SHA256SUMS.txt`).
- Release **v0.2.1** Windows asset is not byte-identical to LFS; no release deletion or history rewrite.
- **User laptop verification** remains explicit **NOT TESTED** in `docs/VERIFICATION_STATUS.md`.
- Future **v0.2.2+** tag may attach LFS-identical MSI if release page must match CI-tested bytes.

## References

- [INSTALL.md](../INSTALL.md), [VERIFICATION_STATUS.md](../VERIFICATION_STATUS.md), [AUDIT.md](../AUDIT.md)
- `desktop/build.gradle.kts`, `desktop/resources/windows/DataWeave-JvmDebug.cmd`
- `.github/workflows/ci.yml`
