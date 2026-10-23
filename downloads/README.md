# DataWeave downloads

## Windows (direct from this repo)

The Windows **MSI** is stored with **Git LFS** (~167 MB; exceeds GitHub’s 100 MB blob limit).

| File | SHA256 |
|------|--------|
| [DataWeave-Setup.msi](DataWeave-Setup.msi) | `130eb799f14bc40d23acb399b5d2d86d6a685765fb8298a42374bdaceddc731c` |

**Direct download (browser):**

https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/DataWeave-Setup.msi

**Verify:**

```bash
sha256sum DataWeave-Setup.msi
# expect: 130eb799f14bc40d23acb399b5d2d86d6a685765fb8298a42374bdaceddc731c
```

See also [SHA256SUMS.txt](SHA256SUMS.txt).

Built from CI run [39990144696](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39990144696) (`:desktop:packageMsi`). Includes `jdk.accessibility` in the bundled JRE (fixes **Failed to launch JVM** when Java Access Bridge is enabled). CI installs the MSI on `windows-latest` and runs `DataWeave.exe --headless-smoke` (with Access Bridge enabled in the test profile).

## All platforms (releases)

**[Download from GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** — Linux, macOS, and future tagged builds.

| Operating system | What to download |
|------------------|------------------|
| Windows | `.msi` (recommended) or `.exe` |
| Linux | `.AppImage` or `.deb` |
| macOS (Apple Silicon) | `.dmg` or `.pkg` from the **macos-arm64** assets |
| macOS (Intel) | `.dmg` or `.pkg` from the **macos-x64** assets |

After downloading from a release, verify checksums using `SHA256SUMS.txt` on the release page.
