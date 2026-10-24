# macOS downloads

| Variant | Status |
|---------|--------|
| **Apple Silicon** | `.dmg` / `.pkg` on [Releases](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest) (`macos-arm64-*`, built on `macos-14` in CI) |
| **Intel (x64)** | **Pending automated CI** — GitHub `macos-13` job omitted (runner queue). Build on an Intel Mac: `./gradlew :desktop:packageDmg :desktop:packagePkg` |

Verify release assets with `SHA256SUMS.txt` on the release page. This folder’s [SHA256SUMS.txt](SHA256SUMS.txt) lists hashes when installers are copied into the repo.
