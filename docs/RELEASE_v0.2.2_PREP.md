# Release v0.2.2 preparation (draft — no tag pushed)

**Status:** Release workflow updated (Windows smoke + LFS MSI alignment on publish). Tag **`v0.2.2`** pushed from repair worker when CI pre-checks pass. **Do not** delete or rewrite [v0.2.1](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.1).

## Why v0.2.2

| Windows artifact | SHA256 | Windows GUI install smoke |
|------------------|--------|---------------------------|
| LFS `downloads/windows/DataWeave-Setup.msi` (canonical) | `35333f603bac8180316934b299623227d2ce35dea5dbf767af92fea45a4c5323` | **Yes** (`ci.yml` @ `main`) |
| Release `windows-DataWeave-1.2.1.msi` (v0.2.1) | `6716b5aeccbe3eefbf8669b375e9c0277ebc08cf3a5c8b679b16f979960e9544` | **No** (`release.yml` packages only) |

End users who follow **Releases → Latest** get the v0.2.1 MSI, not the CI-smoked LFS build. v0.2.2 should align release assets with the LFS MSI (or re-run release build with the same recipe + smoke).

## Pre-tag checklist (maintainer)

1. Confirm `main` CI green (ubuntu + windows + macos-14 + `downloads-verify`).
2. `./gradlew test :desktop:runHeadlessSmoke` and `./scripts/verify-downloads-checksums.sh`.
3. On `windows-latest`, run `:desktop:packageMsi` (or download latest CI `installers-windows-latest` artifact).
4. Verify MSI SHA256 equals LFS file: `35333f603bac8180316934b299623227d2ce35dea5dbf767af92fea45a4c5323` (or update LFS + checksums if intentionally refreshed).
5. Optional hardening before tag: extend `release.yml` Windows job with the same MSI install smoke block as `ci.yml` (copy from `Windows packaged app launch smoke` step).
6. Create annotated tag `v0.2.2` on the commit that contains refreshed LFS/checksums (if any).
7. Let `release.yml` publish; **upload** or replace Windows `.msi` with the LFS-identical file if the workflow build hash differs.
8. Update release notes: canonical Windows download = LFS URL + matching release asset; document retired legacy hashes (`a9c8ab77…`, `b613ec64…`).

## Legacy hashes (do not reinstall)

| SHA256 (prefix) | Meaning |
|-----------------|--------|
| `a9c8ab77…` | Pre-reset LFS MSI (~2026-10-23, `DataWeaveAppKt`, no JvmDebug / stripped `java.exe`) |
| `b613ec64…` | Earlier stale LFS before bootstrap refresh |
| `24f3a23a…` | **134-byte Git LFS pointer** from `raw.githubusercontent.com` — not an installer |
| `6716b5ae…` | v0.2.1 **release** MSI (valid installer, different build run) |

## User-facing canonical URL (unchanged)

https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi

Verify: [downloads/windows/SHA256SUMS.txt](../downloads/windows/SHA256SUMS.txt).
