# PRD v0.3.0 traceability (evidence-based)

Last updated: **2026-10-24 (Asia/Calcutta)** · Branch **`cursor/dataweave-product-v0.3.0-d9c8`**

## Area summary (Must / Partial / Open)

| Area | PRD focus | Code anchor | Automated evidence | Status |
|------|-----------|-------------|-------------------|--------|
| **A** Connections & cred | Lifecycle, scroll, vault persist, recovery | `ConnectionsScreen.kt`, `CredentialRecoveryPanel`, `PostgresConnectionManager` | `ConnectionLifecycleIntegrationTest`, `MissingCredentialIntegrationTest` | **PASS** (Linux CI); Windows GUI **NOT TESTED** |
| **B** Explorer | DB→schema→table/view→columns; PK/FK/index | `DatabaseTreeSidebar.kt`, `CatalogMetadataService` | `MetadataExplorerIntegrationTest`, `ForeignKeyMetadataIntegrationTest`, `ExplorerTableMetadataIntegrationTest` | **PARTIAL** (Columns/PK/FK/Indexes as expandable tree sections; errors per section) |
| **C** Data grid | Spreadsheet read-only, paging, filter, export | `SpreadsheetDataGrid.kt`, `GridSelectionLogic.kt`, `DataWorkspaceScreen.kt` | `PaginationAndTypesIntegrationTest`, `LargeTablePagingIntegrationTest`, `GridSelectionLogicTest` (shift-rect, row/col, Ctrl+C TSV) | **PARTIAL** (matrix improved; manual GUI verify for focus ring) |
| **D** SQL | Read-only SELECT, history, cancel | `SqlEditorScreen.kt`, `ReadOnlyQueryService` | `ReadOnlyQueryServiceTest`, `SqlEditorServiceTest.rejectsNonSelectStatements` | **PARTIAL** (cancel UI; no automated cancel timing test) |
| **E** Datasets | Visual builder + graph persist | `VisualDatasetBuilderScreen.kt`, `DatasetJoinEdgeLayout`, `DatasetGraphValidator` | `DatasetJoinEdgeLayoutTest`, `DatasetGraphValidatorTest`, `DatasetGraphPersistenceIntegrationTest`, join-type SQL tests | **PARTIAL** (canvas join lines + key pickers; DnD **NOT TESTED** in GUI) |
| **F** Projects | CRUD + links + reorder | `ProjectWorkspace.kt`, `ProjectItemService` | `ProjectDatasetNavigationIntegrationTest`, `ProjectItemFeaturesIntegrationTest` | **PARTIAL** (query links, reorder, unsaved dialog; GUI navigation **NOT TESTED**) |
| **G** Settings / theme | Light/Dark/System | `SettingsDialog.kt`, `UserPreferences` | `UserPreferencesTest.themeModeRoundTrip` | **PASS** |
| **H** Reliability | GUI launch, migrations, secrets | `DataWeaveApp.kt` `startForGui`, `SqliteFlywayBackupConfiguration` | `SpringHeadlessPropertyTest`, headless smoke Flyway V5 | **PASS** (Linux); packaged Windows **NOT TESTED** |
| **I** Release | MSI/EXE, checksums, owner sign-off | `release.yml`, `windows-test-installer.yml`, `docs/WINDOWS_RELEASE_ACCEPTANCE.md` | Linux: `./gradlew test :desktop:runHeadlessSmoke :desktop:packageDeb` **PASS** @ `3126f14`; Windows MSI/GUI **NOT TESTED** in Linux VM | **BLOCKED** (Windows acceptance + owner sign-off; no v0.3.0 tag) |

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
