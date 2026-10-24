# DataWeave downloads

Cross-platform layout:

| Folder | Contents |
|--------|----------|
| [windows/](windows/) | **MSI in Git LFS** (~167 MB) — CI-tested GUI/JVM smoke |
| [linux/](linux/) | Release links + checksums when `.deb`/AppImage copied here |
| [macos/](macos/) | Release links (Apple Silicon); Intel build locally |

## Windows (primary in-repo installer)

The Windows **MSI** is stored with **Git LFS** (exceeds GitHub’s 100 MB blob limit).

| File | SHA256 |
|------|--------|
| [windows/DataWeave-Setup.msi](windows/DataWeave-Setup.msi) | `b613ec64d2c30abe613ac2ec821cc0c4443c70c539023b481b33118666dd886e` |

**Direct download (browser):**

https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi

**Portable EXE:** [v0.2.1 release `.exe`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/windows-DataWeave-1.2.1.exe) (not stored in LFS).

**Verify:**

```bash
sha256sum downloads/windows/DataWeave-Setup.msi
# expect: b613ec64d2c30abe613ac2ec821cc0c4443c70c539023b481b33118666dd886e
./scripts/verify-downloads-checksums.sh
```

Built from CI run [39996985661](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/39996985661) (`:desktop:packageMsi`, commit `2ac8c22`). Bundled JRE includes **`java.desktop`** and **`jdk.accessibility`**. CI installs the MSI on `windows-latest` and runs `--diagnose`, `--headless-smoke`, `--gui-smoke`, and a **normal GUI launch** (no args, process alive 15s).

## All platforms (releases)

**[Download from GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** — Linux, macOS, and tagged bundles ([v0.2.1](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.1)). The release **Windows** `.msi` is a separate build from the LFS file (different SHA256); prefer **`downloads/windows/`** if you hit “Failed to launch JVM” on an older or release-only install.

| Operating system | What to download |
|------------------|------------------|
| Windows | LFS `.msi` above or release `.msi` / `.exe` |
| Linux | `.deb` or `.AppImage` — see [linux/README.md](linux/README.md) |
| macOS (Apple Silicon) | `.dmg` / `.pkg` from **macos-arm64** release assets |
| macOS (Intel) | Local build — see [macos/README.md](macos/README.md) |

After downloading from a release, verify checksums using `SHA256SUMS.txt` on the release page and the per-folder files here when artifacts are checked in.
