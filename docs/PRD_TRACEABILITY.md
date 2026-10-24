# PRD v0.3.0 traceability (living)

Last updated: **2026-10-24** · Branch **`cursor/dataweave-product-v0.3.0-d9c8`**

| ID | Requirement (summary) | Implementation | Test / evidence | Status |
|----|----------------------|----------------|----------------|--------|
| FR-021 | Shared workspace context | `WorkspaceContext.kt`, `WorkspaceShell.kt` | Manual + integration via explorer→Data | **Partial** (database context wiring incomplete) |
| FR-100+ | Spreadsheet Data grid | `SpreadsheetDataGrid.kt`, `DataWorkspaceScreen.kt` | Pagination tests; UI manual | **Partial** (no column resize drag; sticky header basic) |
| FR-150+ | Visual Dataset Builder | `DatasetGraphDefinition`, `DatasetGraphService`, builder UI | `DatasetGraphSqlGeneratorTest` | **Partial** (canvas cards not full drag canvas) |
| FR-050+ | Explorer hierarchy | `DatabaseTreeSidebar.kt`, metadata services | Metadata integration tests | **Partial** (views added; keys/indexes not in tree) |
| SEC/SQL | Read-only SQL | `ReadOnlyQueryService`, `SqlEditorService` | `ReadOnlyQueryServiceTest` | **Implemented** |
| Projects | CRUD | `WorkspaceProjectService`, `ProjectWorkspace.kt` | `WorkspaceProjectServiceTest` | **Implemented** (items/refs limited) |
| DM | SQLite backup before migrate | `SqliteFlywayBackupConfiguration` | Headless smoke V4 | **Implemented** (logged backup path) |

Full Must matrix completion is **not** claimed until Windows packaged acceptance and remaining FR rows are verified.
