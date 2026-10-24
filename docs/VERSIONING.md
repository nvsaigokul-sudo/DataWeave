# DataWeave versioning

## Product (GitHub / Gradle)

- **Git tags and releases:** SemVer `v0.3.0`, `v0.2.9`, … on `main`.
- **Gradle `allprojects.version`:** Same numeric product version (e.g. `0.3.0`). Use `-SNAPSHOT` only on unreleased branches.

## jpackage / native installers (platform package version)

Compose Desktop and `jpackage` require a **major version ≥ 1** on macOS and common Windows installer metadata. Product **`0.x.y`** therefore maps to package **`1.x.y`**:

| Gradle product | MSI/EXE/DMG filename | Windows Apps & Settings | Linux `.deb` |
|----------------|----------------------|-------------------------|--------------|
| `0.2.9` | `DataWeave-1.2.9.*` | 1.2.9 | `dataweave_1.2.9_*` |
| `0.3.0` | `DataWeave-1.3.0.*` | 1.3.0 | `dataweave_1.3.0_*` |

Implementation: `jpackageVersion()` in root `build.gradle.kts` and `desktop/build.gradle.kts` (keep in sync).

## Diagnosing mismatches

- Artifact name `TEST-windows-msi-<git-sha>`: `<git-sha>` is the **built commit**, not the product version.
- **`DataWeave-1.2.9.msi` on a v0.3.0 candidate** usually means the workflow ran on **`main` @ v0.2.9**, not the RC branch.
- CI: `./gradlew printProductVersion` prints `dataweave.gradleVersion` and `dataweave.jpackageVersion`.
- Packaged app: `--diagnose` includes `jpackage.app-version` (package version, not Gradle).

## Release gates

- RC / release Windows jobs may set `-PrequireProductVersionPrefix=0.3.0` and `DATAWEAVE_EXPECT_JPACKAGE_VERSION=1.3.0`.
- `GradleProductVersionGateTest` fails if Gradle product version is not `0.3.0*` on the release branch.
