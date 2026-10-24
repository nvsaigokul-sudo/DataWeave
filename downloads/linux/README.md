# Linux downloads

| Artifact | Source |
|----------|--------|
| `.deb` (x64) | [v0.2.4 release](https://github.com/nvsaigokul-sudo/DataWeave/releases/download/v0.2.4/linux-dataweave_1.2.4_amd64.deb) or [Releases latest](https://github.com/nvsaigokul-sudo/DataWeave/releases/latest) |
| AppImage | Release page when published (often >100 MB — not in Git LFS) |

Build locally:

```bash
./gradlew :desktop:packageDeb :desktop:packageAppImage
```

Install smoke:

```bash
sudo dpkg -i desktop/build/compose/binaries/main/deb/dataweave_*.deb
/opt/dataweave/bin/DataWeave --headless-smoke
```

Checksums: [SHA256SUMS.txt](SHA256SUMS.txt) (files verified when present in this directory).
