# DataWeave downloads

## Windows (direct from this repo)

The Windows **MSI** is stored with **Git LFS** (~167 MB; exceeds GitHub’s 100 MB blob limit).

| File | SHA256 |
|------|--------|
| [DataWeave-Setup.msi](DataWeave-Setup.msi) | `a9c8ab77e653c6f202f58ebd877c526e5c220a712eb97f045b5457923d0f06fb` |

**Direct download (browser):**

https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/DataWeave-Setup.msi

**Verify:**

```bash
sha256sum DataWeave-Setup.msi
# expect: a9c8ab77e653c6f202f58ebd877c526e5c220a712eb97f045b5457923d0f06fb
```

See also [SHA256SUMS.txt](SHA256SUMS.txt).

Built from CI run [39987420479](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39987420479) (`:desktop:packageMsi`). Verified on the build agent as **PE/MSI** (Composite Document / Windows Installer); interactive install on Windows was **not** run from the Linux cloud VM.

## All platforms (releases)

**[Download from GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** — Linux, macOS, and future tagged builds.

| Operating system | What to download |
|------------------|------------------|
| Windows | `.msi` (recommended) or `.exe` |
| Linux | `.AppImage` or `.deb` |
| macOS (Apple Silicon) | `.dmg` or `.pkg` from the **macos-arm64** assets |
| macOS (Intel) | `.dmg` or `.pkg` from the **macos-x64** assets |

After downloading from a release, verify checksums using `SHA256SUMS.txt` on the release page.
