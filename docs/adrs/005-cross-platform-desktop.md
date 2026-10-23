# ADR 005: Cross-platform desktop delivery

## Status

Accepted (2026-10-23)

## Decision

- Ship one **Compose Desktop + embedded Spring Boot** application for **Windows, Linux, and macOS** (including Apple Silicon via CI on `macos-14`).
- Select OS-specific **credential vault** and **native packaging** via Gradle host OS source sets (`src/windows`, `src/linux`, `src/macos`) rather than maintaining separate apps.
- Use **`~/.dataweave`** for portable metadata path across OS families.
- Run **GitHub Actions matrix** on ubuntu, windows, and macos for test, smoke, and native package tasks.

## Consequences

- Production packages must be built on each target OS (or CI matrix), not cross-compiled.
- Linux headless CI may use in-memory credential fallback when D-Bus Secret Service is unavailable.
- macOS requires `-XstartOnFirstThread` in Gradle run tasks.
