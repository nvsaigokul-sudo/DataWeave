# PRD v0.3.0 traceability (evidence-based)

Last updated: **2026-10-24 (Asia/Calcutta)** · Gradle **`0.3.0`** · branch **`cursor/dataweave-product-v0.3.0-d9c8`**

## Version / release

| Item | Evidence | Status |
|------|----------|--------|
| Product version | `build.gradle.kts` `version = "0.3.0"` | **PASS** |
| jpackage mapping | `printProductVersion` → `1.3.0`; [VERSIONING.md](./VERSIONING.md) | **PASS** |
| Windows TEST @ wrong commit | `3dbb41c` → `1.2.9` explained | **CLOSED** |
| GitHub Release v0.3.0 | Tag `v0.3.0` + `release.yml` (see release run) | **PASS** (automated); owner GUI **NOT TESTED** |

## Area summary

| Area | PRD focus | Automated evidence | Status |
|------|-----------|-------------------|--------|
| **A** Connections | Lifecycle, vault, recovery | `ConnectionLifecycleIntegrationTest`, `MissingCredentialIntegrationTest` | **PASS** (Linux); Windows GUI **NOT TESTED** |
| **B** Explorer | DB→schema→table; metadata sections | `MetadataExplorerIntegrationTest`, `ExplorerTableMetadataIntegrationTest`, FK tests | **PARTIAL** (sections implemented; views propagation **PARTIAL**) |
| **C** Data grid | Paging, filter, selection, clipboard | `GridSelectionLogicTest`, `LargeTablePagingIntegrationTest`, `PaginationAndTypesIntegrationTest` | **PARTIAL** (no virtualization / multi-tab matrix in automation) |
| **D** SQL | Read-only SELECT, history, cancel | `ReadOnlyQueryServiceTest`, `SqlEditorServiceTest` | **PARTIAL** (cancel timing not automated) |
| **E** Datasets | Visual builder, graph persist | `DatasetGraphValidatorTest`, `DatasetJoinEdgeLayoutTest`, persistence integration tests | **PARTIAL** (GUI DnD **NOT TESTED**) |
| **F** Projects | Links, reorder, navigation | `ProjectItemFeaturesIntegrationTest`, `ProjectDatasetNavigationIntegrationTest` | **PARTIAL** (GUI **NOT TESTED**) |
| **G** Settings / theme | Light/Dark/System | `UserPreferencesTest` | **PASS** |
| **H** Reliability | Migrations, headless smoke | `runHeadlessSmoke`, Flyway V5 | **PASS** (Linux) |
| **I** Release | MSI/EXE, checksums, acceptance | CI run **40112573212** (packaged smoke); owner checklist | **BLOCKED** |

## Selected FR rows

| ID | Summary | Test / evidence | Status |
|----|---------|-----------------|--------|
| FR-021 | Workspace context | `WorkspaceContext.kt` + integration navigation tests | **PARTIAL** |
| SEC | Read-only SQL | `ReadOnlyQueryServiceTest` | **PASS** |
| DM | SQLite backup pre-migrate | Headless smoke migrate | **PASS** |

## Controls

See [controls.csv](./controls.csv). GUI-heavy rows remain **NOT TESTED** or **PARTIAL** until owner Windows pass.

Windows packaged smoke control **WIN-MSI-SMOKE**: **PARTIAL** (CI @ `dd07d22`; must re-verify on release tag commit).
