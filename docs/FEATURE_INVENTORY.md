# DataWeave feature inventory

Last updated: **2026-10-24** (v0.2.7).

| Screen / area | Backend | Desktop UI | Automated test | Manual laptop |
|---------------|---------|------------|----------------|---------------|
| **Home** (`HomeConnectionHub`) | `listConnections`, `connectConnection`, statuses | Saved profiles, Connect/Retry, create/manage | Indirect via lifecycle tests | **PENDING** v0.2.7 |
| **Connections** | Save/Test/Connect/Disconnect/Reconnect, vault | Scrollable form + list, Advanced SSL/timeout | `ConnectionLifecycleIntegrationTest` | Scroll **PASS** v0.2.5 |
| **Explorer** (`DatabaseTreeSidebar`) | Lazy metadata, DB switch, refresh | Object filter, Retry/Refresh, connect CTAs | `MetadataExplorerIntegrationTest` | **PENDING** v0.2.7 |
| **Data / grid** | Paginated browse, filter/sort | `TableDataGrid` | Pagination integration tests | Not verified |
| **SQL** | Read-only queries, history | `SqlEditorScreen` | SQL service tests | Not verified |
| **Datasets** | CSV ingest, preview, output defs | `DatasetBuilderScreen` | Dataset preview cache test | Not verified |
| **Projects** | Local workspace projects | `ProjectWorkspace` | Project service tests | Not verified |
| **Startup** | Embedded Spring, no HTTP server | `startForGui`, timing in `launch.log` | Headless + GUI smoke | Windows CI **PASS** |
| **Packaging** | — | jpackage MSI/DEB/DMG | CI matrix + `RuntimeModuleRequirementsTest` | User install **PENDING** v0.2.7 |

Not implemented (roadmap): M2 pipeline designer, 20M-row virtualization, code signing, sync (M4).
