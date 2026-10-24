# DataWeave audit summary (2026-10-24)

This page is the user-facing audit snapshot. **Verified on `main` @ `b90dcb0`** (2026-10-24 audit worker).

## Verified (automated)

| Area | Status |
|------|--------|
| Linux `./gradlew test :desktop:runHeadlessSmoke` | **Pass** (cloud agent, 2026-10-24) |
| CI `main` matrix (ubuntu / windows / macos-14) | **Pass** ([40063956281](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40063956281); Linux packaged smoke added post-`00c4530`) |
| Windows packaged MSI on CI | **Pass** — `java.desktop` + `jdk.accessibility`, `--diagnose`, `--headless-smoke`, `--gui-smoke`, normal GUI alive 15s |
| LFS Windows MSI SHA256 | `35333f60…` matches [downloads/windows/SHA256SUMS.txt](../downloads/windows/SHA256SUMS.txt) |
| Embedded Spring Boot | **No public HTTP server** (`spring.main.web-application-type=none`) |

## Windows MSI provenance (verified downloads)

| Source | SHA256 | Size | Notes |
|--------|--------|------|-------|
| **Canonical** — LFS + [media.githubusercontent.com](https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi) | `35333f603bac8180316934b299623227d2ce35dea5dbf767af92fea45a4c5323` | 187727956 | CI GUI/JVM smoke on `main`; bootstrap + JvmDebug + `runtime/bin/java.exe` |
| GitHub **Releases → Latest** `windows-DataWeave-1.2.1.msi` (v0.2.1) | `6716b5aeccbe3eefbf8669b375e9c0277ebc08cf3a5c8b679b16f979960e9544` | 187499257 | Valid installer; **different build**; no install smoke in `release.yml` |
| **Legacy LFS** (committed ~2026-10-23, e.g. user laptop `a9c8ab77…`) | `a9c8ab77e653c6f202f58ebd877c526e5c220a712eb97f045b5457923d0f06fb` | ~167 MB | Pre–engineering-reset; `DataWeaveAppKt`, empty `app\resources`, no bundled `java.exe` |
| `raw.githubusercontent.com/.../DataWeave-Setup.msi` | `24f3a23a8d46760578818e9ef2ab62102f558e9d3d9c163247fa454bd3046f17` | 134 | **Git LFS pointer text** — not an installer; use **media** URL or `git lfs pull` |

**Why a user saw `a9c8ab77…` while docs show `35333f60…`:** they installed a **stale LFS-era MSI** (cached browser download, old clone without `git lfs pull`, or partial upgrade under `Program Files\DataWeave`) from before the bootstrap/JvmDebug refresh—not a CDN bug on the current media URL (which serves `35333f60…` as of this audit).

Draft tag workflow to align Releases with LFS: [RELEASE_v0.2.2_PREP.md](./RELEASE_v0.2.2_PREP.md).

## Known limitations (not bugs)

- **Windows release v0.2.1 `.msi`** SHA256 `6716b5ae…` differs from current LFS (separate CI builds). Prefer **`downloads/windows/` LFS MSI** for the build exercised by GUI smoke on `main`.
- **macOS Intel** installers are not built on GitHub Actions (runner queue); build locally on an Intel Mac.
- **Code signing / notarization** not applied.
- **End-user laptops** were not tested in the cloud agent environment.

## Fixes in this audit cycle

- **Query result cache keys** now include SQL and params (avoids rare `Objects.hash` collision returning wrong rows). Regression test: `ReadOnlyQueryServiceTest`.
- **Dataset preview cache keys** include offset/limit (avoids wrong page on cache hit). Regression test: `DatasetPreviewCacheIntegrationTest`.
- **Downloads** reorganized under `downloads/windows|linux|macos/`; CI runs `scripts/verify-downloads-checksums.sh` (rejects accidental LFS pointer files).
- **Engineering reset (Option A):** [ADR 006](./adrs/006-engineering-reset-tdr.md) — bootstrap main class, JvmDebug.cmd, `java.exe` in runtime, Windows CI MSI smoke; Linux `--diagnose` install-root resolution for `.deb` layout.
- **Packaging regression tests:** `RuntimeModuleRequirementsTest`, `LaunchDiagnosticsInstallRootTest` in `:desktop:test`.

## Not implemented / deferred

- v0.2.2 release tag to replace v0.2.1 Windows assets with LFS-identical MSI (documented split; tag only if release page must match LFS byte-for-byte).
- macOS Keychain via CLI (`-w` on command line) — process-list exposure; needs native API for a proper fix.
- Full manual UX sign-off on user Windows/macOS hardware.

See also [PLATFORMS.md](./PLATFORMS.md), [downloads/README.md](../downloads/README.md).
