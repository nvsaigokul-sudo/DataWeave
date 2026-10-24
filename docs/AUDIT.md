# DataWeave audit summary (2026-10-24)

This page is the user-facing audit snapshot (2026-10-24). A longer prioritized report is kept in project coordination notes.

## Verified (automated)

| Area | Status |
|------|--------|
| Linux `./gradlew test :desktop:runHeadlessSmoke` | **Pass** (cloud agent, 2026-10-24) |
| CI `main` matrix (ubuntu / windows / macos-14) | **Pass** ([run 40059507426](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40059507426) after cache fix; [40000887132](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40000887132) prior) |
| Windows packaged MSI on CI | **Pass** — `java.desktop` + `jdk.accessibility`, `--diagnose`, `--headless-smoke`, `--gui-smoke`, normal GUI alive 15s |
| LFS Windows MSI SHA256 | `b613ec64…` matches [downloads/SHA256SUMS.txt](../downloads/SHA256SUMS.txt) |
| Embedded Spring Boot | **No public HTTP server** (`spring.main.web-application-type=none`) |

## Known limitations (not bugs)

- **Windows release v0.2.1 `.msi`** SHA256 `6716b5ae…` differs from LFS `b613ec64…` (separate CI builds). Prefer **LFS MSI** for the build exercised by GUI smoke on `main`.
- **macOS Intel** installers are not built on GitHub Actions (runner queue); build locally on an Intel Mac.
- **Code signing / notarization** not applied.
- **End-user laptops** were not tested in the cloud agent environment.

## Fixes in this audit cycle

- **Query result cache keys** now include SQL and params (avoids rare `Objects.hash` collision returning wrong rows). Regression test: `ReadOnlyQueryServiceTest`.

## Not implemented / deferred

- v0.2.2 release tag to replace v0.2.1 Windows assets with LFS-identical MSI (documented split; tag only if release page must match LFS byte-for-byte).
- macOS Keychain via CLI (`-w` on command line) — process-list exposure; needs native API for a proper fix.
- Full manual UX sign-off on user Windows/macOS hardware.

See also [PLATFORMS.md](./PLATFORMS.md), [downloads/README.md](../downloads/README.md).
