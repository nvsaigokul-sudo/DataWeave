# DataWeave verification status

Last updated: **2026-10-24** (Linux cloud agent, commit `d0d62f3` on `main`).

This page summarizes **what we have actually run**, not roadmap intent. For architecture and audit detail see [AUDIT.md](./AUDIT.md).

## Workflow summary

| Workflow | Scope | Status | Evidence |
|----------|--------|--------|----------|
| `ci.yml` (ubuntu) | test, headless smoke, Linux package | **PASS** | [Run 40061037014](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40061037014) |
| `ci.yml` (windows) | test, MSI/EXE, install, `--diagnose` / `--headless-smoke` / `--gui-smoke`, GUI alive 15s | **PASS** | same run |
| `ci.yml` (macos-14) | test, headless smoke, macOS package | **PASS** | same run |
| `release.yml` @ v0.2.1 | Linux/Windows/macOS artifacts | **PASS** (build) | [39998843720](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39998843720) — **no** Windows GUI install smoke |
| Local Gradle (Linux VM) | `./gradlew test :desktop:runHeadlessSmoke` | **PASS** | agent 2026-10-24 |
| Linux `.deb` install smoke | `/opt/dataweave/bin/DataWeave` CLI modes | **PASS** | packaged `@ main` tree, SHA256 `38be7af0…` |
| Linux interactive UI (connections wizard) | Full Compose UX | **NOT TESTED** | Xvfb `--gui-smoke` only |
| User Windows/macOS laptop | Manual install | **NOT TESTED** | cloud constraint |

## PostgreSQL end-to-end

| Check | Status | Notes |
|-------|--------|--------|
| Embedded PG in integration tests | **PASS** | Zonky; not user production DB |
| Multi-page + NULLs + types fixture | **PASS** | `PaginationAndTypesIntegrationTest` |
| Docker PG on agent VM | **BLOCKED** | `docker` not installed |
| GUI “add connection” flow | **NOT TESTED** | backend path covered by tests + headless smoke |

## Security / cache (recent fixes)

| Item | Status |
|------|--------|
| Read-only query cache keys include SQL text | **PASS** on `main` |
| Table page cache keys include offset/limit/schema version | **PASS** (existing) |

## Download checksums (Windows)

| File | SHA256 |
|------|--------|
| LFS `downloads/DataWeave-Setup.msi` (CI GUI-tested) | `b613ec64d2c30abe613ac2ec821cc0c4443c70c539023b481b33118666dd886e` |
| Release `windows-DataWeave-1.2.1.msi` | `6716b5aeccbe3eefbf8669b375e9c0277ebc08cf3a5c8b679b16f979960e9544` |

Different hashes reflect **different build runs**; README explains when to prefer the LFS MSI.

## Release readiness (current slice)

**Ready** for: installable desktop app, PostgreSQL connection/browse/pagination for the implemented UI, automated CI on `main`.

**Not ready / not claimed:** dataset builder, large-scale grid performance, code signing, notarized macOS builds, release-workflow parity with Windows GUI smoke.

## How to reproduce (developers)

```bash
./gradlew test :desktop:runHeadlessSmoke
./gradlew :desktop:packageDeb
sudo dpkg -i desktop/build/compose/binaries/main/deb/dataweave_1.2.1_amd64.deb
/opt/dataweave/bin/DataWeave --headless-smoke
```

Internal agent report: Project store `internal/dataweave-e2e-validation-2026-10-24.md`.
