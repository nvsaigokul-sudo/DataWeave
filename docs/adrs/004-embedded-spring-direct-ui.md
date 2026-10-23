# ADR 004: Embedded Spring Boot with direct UI wiring

## Status

Accepted (2026-10-23)

## Context

DataWeave needs structured services (connection pooling, caching, JDBC, Flyway) without operating a separate server process or exposing LAN listeners.

## Decision

- Run **Spring Boot 3.4** inside the desktop JVM with `WebApplicationType.NONE` (no HTTP server).
- Kotlin Compose calls **`ApplicationFacade`** beans via **`DesktopSession`** (direct method calls, no localhost HTTP).
- **SQLite** (`~/.dataweave/metadata.db`) stores local connection profiles and workspace metadata via Flyway.
- User **PostgreSQL** databases are accessed through per-connection Hikari pools.

## Consequences

- Clean layering: UI → facade → services → cache/JDBC.
- No open TCP port for UI/backend traffic on loopback or LAN.
- Windows builds can compile `WindowsCredentialVault` (Credential Manager); Linux CI uses in-memory vault with warnings.

## Rejected

- Separate Spring Boot server the user must start manually.
- Electron/Tauri shell with HTTP-only backend (higher complexity, harder JDBC sharing).
