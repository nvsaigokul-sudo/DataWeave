# ADR 003: Desktop UI stack selection

## Status

Accepted (2026-10-23)

## Context

DataWeave targets Windows desktop with a modern UI, JVM-friendly data libraries, and room for a high-performance preview grid.

## Options considered

| Criterion | JavaFX | Compose Desktop | Tauri | Electron |
|-----------|--------|-----------------|-------|----------|
| Windows fit | Strong native installers, mature | Strong via JVM packaging | Good (WebView2) | Good |
| Offline / local I/O | Excellent (same process as JVM) | Excellent | Good (Rust + WebView) | Good (Node) |
| PostgreSQL/JDBC | Native | Native | Needs sidecar/FFI | Sidecar or WASM bridge |
| Performance for large grids | Good with VirtualFlow | Good with Lazy lists | Depends on web UI | Heavier memory |
| UI velocity | Moderate | High (declarative) | High (web stack) | Highest (web) |
| Bundle size | Moderate JRE | Moderate JRE | Smaller | Larger (Chromium) |
| Team JVM alignment | High | High | Low | Low |

## Decision

**Kotlin 2 + Compose Desktop + JVM 21**, sharing libraries with future headless execution services.

Rejected for v0.1:

- **Electron**: higher baseline memory and weaker single-process JDBC story.
- **Tauri**: excellent shell but splits UI/engine across Rust/TS boundary early.
- **JavaFX**: viable fallback; Compose chosen for declarative UI speed and Material theming.

## Consequences

- MSI builds include JRE/runtime via Compose tooling.
- macOS/Linux dev hosts supported; Windows remains primary QA target.
- Revisit if WebGPU-heavy visualizations require embedded browser (unlikely for grid-first UX).
