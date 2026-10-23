# DataWeave downloads

## Windows (direct from this repo)

The Windows **MSI** is stored with **Git LFS** (~167 MB; exceeds GitHub’s 100 MB blob limit).

| File | SHA256 |
|------|--------|
| [DataWeave-Setup.msi](DataWeave-Setup.msi) | `eea8bcee1b0848ae9d4a7b960ecc997d4c6498442289c55b5857910119f610ac` |

**Direct download (browser):**

https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/DataWeave-Setup.msi

**Verify:**

```bash
sha256sum DataWeave-Setup.msi
# expect: eea8bcee1b0848ae9d4a7b960ecc997d4c6498442289c55b5857910119f610ac
```

See also [SHA256SUMS.txt](SHA256SUMS.txt).

Built from CI run [39995020267](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39995020267) (`:desktop:packageMsi`). Bundled JRE includes `jdk.accessibility` and `jdk.localedata`. CI installs the MSI on `windows-latest` and runs `--diagnose`, `--headless-smoke`, and `--gui-smoke` (Compose/Skiko; Access Bridge enabled in the test profile).

## All platforms (releases)

**[Download from GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** — Linux, macOS, and future tagged builds.

| Operating system | What to download |
|------------------|------------------|
| Windows | `.msi` (recommended) or `.exe` |
| Linux | `.AppImage` or `.deb` |
| macOS (Apple Silicon) | `.dmg` or `.pkg` from the **macos-arm64** assets |
| macOS (Intel) | `.dmg` or `.pkg` from the **macos-x64** assets |

After downloading from a release, verify checksums using `SHA256SUMS.txt` on the release page.
