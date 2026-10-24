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
| [windows/DataWeave-Setup.msi](windows/DataWeave-Setup.msi) | `35333f603bac8180316934b299623227d2ce35dea5dbf767af92fea45a4c5323` |

**Direct download (browser):**

https://media.githubusercontent.com/media/nvsaigokul-sudo/DataWeave/main/downloads/windows/DataWeave-Setup.msi

**Portable EXE:** [v0.2.1 release `.exe`](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.1/windows-DataWeave-1.2.1.exe) (not stored in LFS).

**Verify:**

```bash
sha256sum downloads/windows/DataWeave-Setup.msi
# expect: 35333f603bac8180316934b299623227d2ce35dea5dbf767af92fea45a4c5323
./scripts/verify-downloads-checksums.sh
```

**Mandatory reinstall:** If your installed copy has SHA256 `b613ec64…` (or `app\resources` is empty / `DataWeave.cfg` lists `DataWeaveAppKt`), uninstall the old MSI and install this build. Older LFS MSIs predated bootstrap + `DataWeave-JvmDebug.cmd`.

Built from CI run [40063457806](https://github.com/nvsaigokul-sudo/DataWeave/actions/runs/40063457806) (`:desktop:packageMsi`, commit `34b832f`). Includes **`DataWeaveBootstrapKt`**, **`app/resources/DataWeave-JvmDebug.cmd`**, bundled **`runtime/bin/java.exe`**, and JRE modules **`java.desktop`** + **`jdk.accessibility`**. CI installs the MSI on `windows-latest` and runs `--diagnose`, `DataWeave-JvmDebug.cmd --diagnose`, `--headless-smoke`, `--gui-smoke`, and a **normal GUI launch** (no args, process alive 15s).

## All platforms (releases)

**[Download from GitHub Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest)** — Linux, macOS, and tagged bundles ([v0.2.1](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.2.1)). The release **Windows** `.msi` is a separate build from the LFS file (different SHA256); prefer **`downloads/windows/`** if you hit “Failed to launch JVM” on an older or release-only install.

| Operating system | What to download |
|------------------|------------------|
| Windows | LFS `.msi` above or release `.msi` / `.exe` |
| Linux | `.deb` or `.AppImage` — see [linux/README.md](linux/README.md) |
| macOS (Apple Silicon) | `.dmg` / `.pkg` from **macos-arm64** release assets |
| macOS (Intel) | Local build — see [macos/README.md](macos/README.md) |

After downloading from a release, verify checksums using `SHA256SUMS.txt` on the release page and the per-folder files here when artifacts are checked in.
