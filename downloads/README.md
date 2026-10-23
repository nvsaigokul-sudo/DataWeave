# DataWeave downloads

## Windows (direct from this repo)

The Windows **MSI** is stored with **Git LFS** (~167 MB; exceeds GitHub’s 100 MB blob limit).

| File | SHA256 |
|------|--------|
| [DataWeave-Setup.msi](DataWeave-Setup.msi) | `b613ec64d2c30abe613ac2ec821cc0c4443c70c539023b481b33118666dd886e` |

**Direct download (browser):**

https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/DataWeave-Setup.msi

**Verify:**

```bash
sha256sum DataWeave-Setup.msi
# expect: b613ec64d2c30abe613ac2ec821cc0c4443c70c539023b481b33118666dd886e
```

See also [SHA256SUMS.txt](SHA256SUMS.txt).

Built from CI run [39996985661](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39996985661) (`:desktop:packageMsi`, commit `2ac8c22`). Bundled JRE includes **`java.desktop`** and **`jdk.accessibility`**. CI installs the MSI on `windows-latest` and runs `--diagnose`, `--headless-smoke`, `--gui-smoke`, and a **normal GUI launch** (no args, process alive 15s). Replaces older installers (e.g. `130eb799…`) that omitted `java.desktop` from the explicit jlink module list.

## All platforms (releases)

**[Download from GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** — Linux, macOS, and tagged bundles ([v0.2.1](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.1)). The release **Windows** `.msi` is a separate build from this LFS file (different SHA256); prefer **this page’s MSI** if you hit “Failed to launch JVM” on an older or release-only install.

| Operating system | What to download |
|------------------|------------------|
| Windows | `.msi` (recommended) or `.exe` |
| Linux | `.AppImage` or `.deb` |
| macOS (Apple Silicon) | `.dmg` or `.pkg` from the **macos-arm64** assets |
| macOS (Intel) | `.dmg` or `.pkg` from the **macos-x64** assets |

After downloading from a release, verify checksums using `SHA256SUMS.txt` on the release page.
