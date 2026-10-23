# Release process

## Versioning

SemVer tags on `main`, e.g. `v0.2.0`. Project version: `0.2.0-SNAPSHOT` in Gradle until tagged.

## Automated release

Push a tag:

```bash
git tag v0.2.0
git push github v0.2.0
```

Or run **Release** workflow manually (`workflow_dispatch`) from GitHub Actions.

Workflow [.github/workflows/release.yml](../.github/workflows/release.yml):

1. Tests + headless smoke on each runner
2. Native packages per OS (jpackage via Compose Desktop, **includes bundled JRE**)
3. Upload artifacts + `SHA256SUMS.txt` to GitHub Release

| Runner | Artifacts |
|--------|-----------|
| ubuntu-latest | AppImage, `.deb`, portable `tar.gz` of app dir |
| windows-latest | `.msi`, `.exe` |
| macos-14 | `.dmg`, `.pkg` (Apple Silicon) |
| macos-13 | `.dmg`, `.pkg` (Intel) |

## Signing / notarization (honest status)

- **Default:** binaries are **not** Authenticode-signed, **not** Apple-notarized.
- Document any signed release explicitly in release notes.
- Users should verify `SHA256SUMS.txt` from the release page.

## Pre-release checklist

- [ ] `./gradlew test :desktop:runHeadlessSmoke` green on `main`
- [ ] No secrets or credentials in git
- [ ] Tag matches Gradle `packageVersion` (see `desktop/build.gradle.kts`)
- [ ] All matrix jobs green before publishing (workflow fails if an artifact is missing)

## Binaries in git

Do **not** commit installers to the repository. Use GitHub Releases only.
