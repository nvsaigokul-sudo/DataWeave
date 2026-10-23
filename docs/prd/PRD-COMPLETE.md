# DataWeave Product Requirements Document
Canonical index: docs/prd/sections/

## Section 01: Executive Summary

DataWeave is an offline-first Windows desktop workbench for importing, transforming, and exporting structured data. Local PostgreSQL stores project metadata, pipeline definitions, and execution history. Session 1 delivers the product foundation, documentation pack, technology decision, and a vertical slice: project workspaces persisted in PostgreSQL with a Compose Desktop shell.

## Section 02: Document Control

Owner: DataWeave product team. Status: Draft 0.1. Audience: engineering, QA, security review. Canonical PRD path: docs/prd/PRD-COMPLETE.md and docs/prd/sections/.

## Section 03: Product Vision

Empower analysts and data engineers to work confidently on sensitive datasets without continuous cloud connectivity, while retaining optional sync when online.

## Section 04: Problem Statement

Teams need repeatable data pipelines but cannot always depend on SaaS ETL platforms. Existing desktop tools are either cloud-tethered, weak on performance at scale, or lack a durable local catalog.

## Section 05: Goals and Non-Goals

Goals: offline-first UX, PostgreSQL as system of record, high-performance preview grid (future), Windows desktop packaging. Non-goals (v0.1): multi-user server, real-time collaboration, cloud execution cluster, MuleSoft DataWeave language compatibility.

## Section 06: Personas

Primary: Data Analyst (Excel/CSV heavy). Secondary: Integration Engineer (JDBC sources). Tertiary: IT Admin (install, backup, updates).

## Section 07: Jobs to Be Done

Register sources, map fields, validate samples, run transforms, export results, resume work offline, optionally publish when connected.

## Section 08: Product Principles

Local-first authority, explicit sync, measurable performance budgets, no fabricated benchmarks, least-privilege DB access, testable vertical slices.

## Section 09: Scope Overview

v0.1: projects, PostgreSQL metadata, app shell, docs/ADRs. v0.2: CSV ingest + preview grid. v0.3: pipeline graph + execution engine.

## Section 10: FR: Workspace

Users create named projects with descriptions. Projects list sorted by recent activity. Delete project (with confirmation in future slice).

## Section 11: FR: Connections

Define connection profiles (folder, JDBC) stored encrypted locally (future). v0.1 placeholder navigation only.

## Section 12: FR: Pipelines

Directed acyclic transform graphs with typed nodes (source, map, filter, sink). Persisted as JSON in PostgreSQL (future).

## Section 13: FR: Preview & Grid

Virtualized grid for sample rows with column stats. Budget: initial 10k-row sample under interactive thresholds (TBD measurement).

## Section 14: FR: Export

Export to CSV/Parquet/SQL script with deterministic ordering. Background job with cancel (future).

## Section 15: FR: Optional Sync

When online, push/pull project bundles to optional remote registry. Never required for core workflows.

## Section 16: Non-Functional Requirements

Availability offline, durability via PostgreSQL, Windows 10/11 x64, installer via MSI, auto-update channel (future).

## Section 17: Offline-First Behavior

All create/read/update paths for v0.1 projects operate without network. Sync modules fail closed when offline.

## Section 18: Performance Requirements

See docs/performance-budget.md. No numeric SLA claimed until measured on reference hardware.

## Section 19: Security Requirements

See docs/security.md. Secrets never in git. OS credential store for production connection strings (future).

## Section 20: Privacy & Compliance

Data remains on customer machines by default. Telemetry opt-in only (future). Support GDPR export/delete at project level (future).

## Section 21: Platform & Compatibility

Primary target: Windows 10/11 x64 desktop. Dev builds run on Linux/macOS via Compose Desktop. PostgreSQL 14+.

## Section 22: Data Model Overview

projects table (v1 migration). Future: sources, pipelines, runs, artifacts, connection_vault.

## Section 23: Integration Requirements

JDBC drivers bundled selectively. File imports: CSV first. Optional S3/Azure sync adapters (defer).

## Section 24: UX Requirements

Calm sidebar navigation, empty states, inline errors, keyboard focus on primary actions.

## Section 25: Accessibility

Target WCAG 2.1 AA for core flows: contrast, focus order, screen reader labels on primary controls (incremental).

## Section 26: Localization

English-only v0.1. Externalize strings for future locales.

## Section 27: Observability

Structured logs locally, diagnostic bundle export (future). No cloud logging by default.

## Section 28: Migration & Upgrade

Flyway versioned migrations on startup. Backup guidance before upgrade in RELEASE.md.

## Section 29: Support & Documentation

DEVELOPMENT.md for contributors, user guide (future), in-app link to docs folder.

## Section 30: Success Metrics

Time-to-first-project, preview latency (measured), crash-free sessions, install success rate on Windows reference VMs.

## Section 31: Release Criteria

See docs/RELEASE.md. All blocking tests green, MSI smoke on Windows agent, migration forward-only verified.

## Section 32: Test Strategy Reference

See docs/test-strategy.md. Unit + embedded PostgreSQL integration + headless smoke + manual GUI checklist on Windows.

## Section 33: Traceability Reference

See docs/traceability-matrix.md mapping FR IDs to tests and migrations.

## Section 34: Dependencies & Assumptions

Assumes local PostgreSQL instance or embedded dev mode. JVM 21+. Customers provide Windows admin for MSI install.

## Section 35: Constraints

Offline-first, PostgreSQL, no secrets in repo, no fabricated performance numbers.

## Section 36: Risks

See docs/risks-and-backlog.md.

## Section 37: Open Questions

Bundled PostgreSQL vs customer-managed? Per-project DB vs shared catalog DB? Licensing for optional JDBC drivers.

## Section 38: Glossary

Project: top-level workspace. Pipeline: transform DAG. Slice: shippable vertical increment.

## Section 39: Appendices

Appendix A: reference hardware for future benchmarks (Windows 11, 16GB RAM, NVMe). Appendix B: sample CSV fixtures (future).

## Section 40: Approval

Pending product owner sign-off.

## Section 41: Revision History

2026-10-23: Session 1 initial PRD pack and v0.1 slice.

