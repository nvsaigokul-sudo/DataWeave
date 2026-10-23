# Cross-platform verification

Do not mark a platform **Verified** until the listed commands succeed on that OS.

## All platforms

```bash
./gradlew test
./gradlew :desktop:runHeadlessSmoke
```

Expect: `BUILD SUCCESSFUL` and `HEADLESS_SMOKE_OK`.

## Windows (PowerShell)

```powershell
.\gradlew.bat test
.\gradlew.bat :desktop:runHeadlessSmoke
.\gradlew.bat :desktop:run --args="--dev"
.\gradlew.bat :desktop:packageMsi
```

Check Credential Manager entries under `DataWeave/…` and `%USERPROFILE%\.dataweave\metadata.db` (no plaintext passwords).

## Linux

```bash
./gradlew test :desktop:runHeadlessSmoke
./gradlew :desktop:run --args="--dev"
./gradlew :desktop:packageDeb
# optional AppImage:
./gradlew :desktop:packageAppImage
```

If `secret-tool` is installed, confirm keyring storage; otherwise expect WARN and session memory fallback.

## macOS

```bash
./gradlew test :desktop:runHeadlessSmoke
./gradlew :desktop:run --args="--dev"
./gradlew :desktop:packageDmg
```

Confirm Keychain entry via Keychain Access (service `DataWeave/…`). On Apple Silicon, prefer native JDK 21 (aarch64).

## Network

While running, confirm **no** Spring HTTP listener (embedded backend uses `WebApplicationType.NONE`).
