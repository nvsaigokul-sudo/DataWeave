# PRD v0.3.0 traceability (evidence-based)

Last updated: **2026-10-24 (UTC)** · Branch **`cursor/dataweave-product-v0.3.0-d9c8`**

## Area summary (Must / Partial / Open)

| Area | PRD focus | Code anchor | Automated evidence | Status |
|------|-----------|-------------|-------------------|--------|
| **A** Connections & cred | Lifecycle, scroll, vault persist, recovery | `ConnectionsScreen.kt`, `CredentialRecoveryPanel`, `PostgresConnectionManager` | `ConnectionLifecycleIntegrationTest`, `MissingCredentialIntegrationTest` | **PASS** (Linux CI); Windows GUI **NOT TESTED** |
| **B** Explorer | DB→schema→table/view→columns; PK/FK/index | `DatabaseTreeSidebar.kt`, `CatalogMetadataService` | `MetadataExplorerIntegrationTest`, `ForeignKeyMetadataIntegrationTest`, `ExplorerTableMetadataIntegrationTest` | **PARTIAL** (PK/FK/index under expanded table; no separate FK tree nodes) |
| **C** Data grid | Spreadsheet read-only, paging, filter, export | `SpreadsheetDataGrid.kt`, `DataWorkspaceScreen.kt` | `PaginationAndTypesIntegrationTest`, `TableBrowseColumnFilterIntegrationTest`, `TableExportServiceTest` | **PARTIAL** (keyboard matrix, full virtualization vs PRD) |
| **D** SQL | Read-only SELECT, history, cancel | `SqlEditorScreen.kt`, `ReadOnlyQueryService` | `ReadOnlyQueryServiceTest`, `SqlEditorServiceTest.rejectsNonSelectStatements` | **PARTIAL** (cancel UI; no automated cancel timing test) |
| **E** Datasets | Visual builder + graph persist | `VisualDatasetBuilderScreen.kt`, `DatasetGraphService` | `DatasetGraphPreviewIntegrationTest`, `DatasetGraphSqlGeneratorTest`, `JoinPreviewServiceTest` | **PARTIAL** (join keys editable; drag DnD limited to buttons + canvas move) |
| **F** Projects | CRUD local metadata | `ProjectWorkspace.kt`, `WorkspaceProjectService` | `WorkspaceProjectServiceTest` | **PASS** (project items/refs to datasets not full PRD) |
| **G** Settings / theme | Light/Dark/System | `SettingsDialog.kt`, `UserPreferences` | `UserPreferencesTest.themeModeRoundTrip` | **PASS** |
| **H** Reliability | GUI launch, migrations, secrets | `DataWeaveApp.kt` `startForGui`, `SqliteFlywayBackupConfiguration` | `SpringHeadlessPropertyTest`, headless smoke Flyway V4 | **PASS** (Linux); packaged Windows **NOT TESTED** |
| **I** Release | MSI/EXE, checksums, owner sign-off | `release.yml`, Gradle `0.3.0-SNAPSHOT` | `:desktop:packageDeb` **PASS** (Linux deb only this run) | **BLOCKED** (no v0.3.0 tag; no Windows MSI from this commit) |

## Requirement rows (selected FR)

| ID | Summary | Implementation | Test | Status |
|----|---------|----------------|------|--------|
| FR-021 | Workspace context | `WorkspaceContext.kt`, shell wiring, `activeDatabase` | Manual cross-screen | **PARTIAL** |
| FR-050+ | Explorer metadata | Tables, views, columns, PK, FK, indexes (lazy) | See area B tests | **PARTIAL** |
| FR-100+ | Data workspace | Spreadsheet grid + tabs | Pagination + column filter tests | **PARTIAL** |
| FR-150+ | Visual dataset builder | Canvas, graph JSON save/reopen | Graph preview + SQL generator tests | **PARTIAL** |
| SEC | Read-only SQL | `BEGIN READ ONLY` | `ReadOnlyQueryServiceTest`, `SqlEditorServiceTest` | **PASS** |
| DM | SQLite backup pre-migrate | `SqliteFlywayBackupConfiguration` | Headless smoke migrate | **PASS** |

Windows packaged GUI acceptance remains **NOT TESTED** in cloud agent environment.
