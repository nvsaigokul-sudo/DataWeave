package com.dataweave.backend;

import com.dataweave.backend.cache.CacheDiagnosticsService;
import com.dataweave.backend.cache.CacheInvalidationService;
import com.dataweave.backend.domain.ConnectionProfile;
import com.dataweave.backend.domain.ConnectionSessionStatus;
import com.dataweave.backend.domain.ConnectionTestResult;
import com.dataweave.backend.domain.CsvSource;
import com.dataweave.backend.domain.DataPage;
import com.dataweave.backend.domain.SqlQueryHistoryEntry;
import com.dataweave.backend.domain.SslMode;
import com.dataweave.backend.domain.TableBrowseOptions;
import com.dataweave.backend.domain.TableExportResult;
import com.dataweave.backend.domain.TableRef;
import com.dataweave.backend.domain.WorkspaceProject;
import com.dataweave.backend.postgres.PostgresConnectionManager;
import com.dataweave.backend.service.CatalogMetadataService;
import com.dataweave.backend.service.ConnectionProfileService;
import com.dataweave.backend.service.ConnectionSessionService;
import com.dataweave.backend.domain.DatasetOutputDefinition;
import com.dataweave.backend.domain.ForeignKeyRef;
import com.dataweave.backend.domain.JoinType;
import com.dataweave.backend.service.CsvIngestService;
import com.dataweave.backend.domain.DatasetGraphDefinition;
import com.dataweave.backend.service.DatasetGraphService;
import com.dataweave.backend.service.DatasetOutputDefinitionService;
import com.dataweave.backend.service.DatasetPreviewService;
import com.dataweave.backend.service.JoinPreviewService;
import com.dataweave.backend.service.ReadOnlyQueryService;
import com.dataweave.backend.service.SqlEditorService;
import com.dataweave.backend.service.TableDataService;
import com.dataweave.backend.service.TableExportService;
import com.dataweave.backend.service.WorkspaceProjectService;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;

@Component
public class ApplicationFacade {

    private final WorkspaceProjectService workspaceProjectService;
    private final ConnectionProfileService connectionProfileService;
    private final ConnectionSessionService connectionSessionService;
    private final CatalogMetadataService catalogMetadataService;
    private final TableDataService tableDataService;
    private final DatasetPreviewService datasetPreviewService;
    private final ReadOnlyQueryService readOnlyQueryService;
    private final SqlEditorService sqlEditorService;
    private final CsvIngestService csvIngestService;
    private final DatasetOutputDefinitionService datasetOutputDefinitionService;
    private final DatasetGraphService datasetGraphService;
    private final JoinPreviewService joinPreviewService;
    private final TableExportService tableExportService;
    private final CacheDiagnosticsService cacheDiagnosticsService;
    private final CacheInvalidationService cacheInvalidationService;
    private final PostgresConnectionManager postgresConnectionManager;

    public ApplicationFacade(
            WorkspaceProjectService workspaceProjectService,
            ConnectionProfileService connectionProfileService,
            ConnectionSessionService connectionSessionService,
            CatalogMetadataService catalogMetadataService,
            TableDataService tableDataService,
            DatasetPreviewService datasetPreviewService,
            ReadOnlyQueryService readOnlyQueryService,
            SqlEditorService sqlEditorService,
            CsvIngestService csvIngestService,
            DatasetOutputDefinitionService datasetOutputDefinitionService,
            DatasetGraphService datasetGraphService,
            JoinPreviewService joinPreviewService,
            TableExportService tableExportService,
            CacheDiagnosticsService cacheDiagnosticsService,
            CacheInvalidationService cacheInvalidationService,
            PostgresConnectionManager postgresConnectionManager) {
        this.workspaceProjectService = workspaceProjectService;
        this.connectionProfileService = connectionProfileService;
        this.connectionSessionService = connectionSessionService;
        this.catalogMetadataService = catalogMetadataService;
        this.tableDataService = tableDataService;
        this.datasetPreviewService = datasetPreviewService;
        this.readOnlyQueryService = readOnlyQueryService;
        this.sqlEditorService = sqlEditorService;
        this.csvIngestService = csvIngestService;
        this.datasetOutputDefinitionService = datasetOutputDefinitionService;
        this.datasetGraphService = datasetGraphService;
        this.joinPreviewService = joinPreviewService;
        this.tableExportService = tableExportService;
        this.cacheDiagnosticsService = cacheDiagnosticsService;
        this.cacheInvalidationService = cacheInvalidationService;
        this.postgresConnectionManager = postgresConnectionManager;
    }

    public List<WorkspaceProject> listProjects() {
        return workspaceProjectService.list();
    }

    public WorkspaceProject createProject(String name, String description) {
        return workspaceProjectService.create(name, description);
    }

    public WorkspaceProject updateProject(UUID id, String name, String description) {
        return workspaceProjectService.update(id, name, description);
    }

    public WorkspaceProject duplicateProject(UUID id) {
        return workspaceProjectService.duplicate(id);
    }

    public void deleteProject(UUID id) {
        workspaceProjectService.delete(id);
    }

    public List<ConnectionProfile> listConnections() {
        return connectionProfileService.list();
    }

    public ConnectionProfile saveConnection(
            String name,
            String host,
            int port,
            String database,
            String username,
            char[] password,
            SslMode sslMode,
            int timeoutSeconds) {
        return connectionProfileService.create(name, host, port, database, username, password, sslMode, timeoutSeconds);
    }

    public ConnectionProfile updateConnection(
            UUID id,
            String name,
            String host,
            int port,
            String database,
            String username,
            char[] passwordOrNull,
            SslMode sslMode,
            int timeoutSeconds) {
        ConnectionProfile updated =
                connectionProfileService.update(
                        id, name, host, port, database, username, passwordOrNull, sslMode, timeoutSeconds);
        connectionSessionService.markDisconnected(id);
        return updated;
    }

    public ConnectionTestResult testConnection(UUID id, char[] passwordOverride) {
        return connectionProfileService.test(id, passwordOverride);
    }

    public boolean hasStoredCredential(UUID connectionId) {
        return connectionProfileService.hasStoredCredential(connectionId);
    }

    public ConnectionSessionStatus connectConnection(UUID id) {
        return connectionSessionService.connect(id);
    }

    public ConnectionSessionStatus connectConnection(UUID id, char[] passwordOverride) {
        return connectionSessionService.connect(id, passwordOverride);
    }

    public ConnectionSessionStatus disconnectConnection(UUID id) {
        return connectionSessionService.disconnect(id);
    }

    public ConnectionSessionStatus reconnectConnection(UUID id) {
        return connectionSessionService.reconnect(id);
    }

    public ConnectionSessionStatus connectionStatus(UUID id) {
        return connectionSessionService.status(id);
    }

    public List<ConnectionSessionStatus> allConnectionStatuses() {
        return connectionSessionService.allStatuses();
    }

    public void deleteConnection(UUID id) {
        connectionSessionService.disconnect(id);
        connectionProfileService.delete(id);
    }

    public long refreshSchema(UUID connectionId) {
        return connectionProfileService.refreshSchema(connectionId);
    }

    public ConnectionSessionStatus switchConnectionDatabase(UUID connectionId, String databaseName) {
        connectionProfileService.switchDatabase(connectionId, databaseName);
        connectionSessionService.markDisconnected(connectionId);
        return connectionSessionService.connect(connectionId);
    }

    public List<String> listDatabases(UUID connectionId) throws Exception {
        return catalogMetadataService.listDatabases(connectionId);
    }

    public List<String> listSchemas(UUID connectionId) throws Exception {
        return catalogMetadataService.listSchemas(connectionId);
    }

    public List<TableRef> listTables(UUID connectionId, String schema) throws Exception {
        return catalogMetadataService.listTables(connectionId, schema);
    }

    public List<TableRef> listViews(UUID connectionId, String schema) throws Exception {
        return catalogMetadataService.listViews(connectionId, schema);
    }

    public List<com.dataweave.backend.domain.ColumnMeta> listColumns(UUID connectionId, String schema, String table)
            throws Exception {
        return catalogMetadataService.listColumns(connectionId, schema, table);
    }

    public CompletableFuture<DataPage> fetchTablePage(
            UUID connectionId, TableRef table, long offset, Integer limit, String requestScope) {
        return fetchTablePage(connectionId, table, offset, limit, requestScope, TableBrowseOptions.none());
    }

    public CompletableFuture<DataPage> fetchTablePage(
            UUID connectionId,
            TableRef table,
            long offset,
            Integer limit,
            String requestScope,
            TableBrowseOptions options) {
        return tableDataService.fetchPageAsync(connectionId, table, offset, limit, requestScope, options);
    }

    public long beginDatasetPreview(String scopeKey) {
        return datasetPreviewService.beginPreviewScope(scopeKey);
    }

    public CompletableFuture<DataPage> previewDataset(
            String scopeKey, UUID connectionId, TableRef table, List<String> columns, long offset, int limit) {
        return datasetPreviewService.previewAsync(scopeKey, connectionId, table, columns, offset, limit);
    }

    public List<Map<String, Object>> runSelect(UUID connectionId, String sql, List<Object> params) throws Exception {
        return readOnlyQueryService.runSelect(connectionId, sql, params);
    }

    public DataPage runSqlEditorQuery(UUID connectionId, String sql) throws Exception {
        return sqlEditorService.executeSelect(connectionId, sql);
    }

    public List<SqlQueryHistoryEntry> sqlQueryHistory(int limit) {
        return sqlEditorService.history(limit);
    }

    public CsvSource registerCsvSource(UUID projectId, String name, Path filePath) throws Exception {
        return csvIngestService.register(projectId, name, filePath);
    }

    public List<CsvSource> listCsvSources(UUID projectId) {
        return csvIngestService.listForProject(projectId);
    }

    public List<DatasetOutputDefinition> listDatasetDefinitions(UUID projectId) {
        return datasetOutputDefinitionService.listForProject(projectId);
    }

    public DatasetOutputDefinition saveDatasetDefinition(
            UUID idOrNull,
            UUID projectId,
            UUID connectionId,
            String schema,
            String table,
            List<String> outputColumns)
            throws Exception {
        return datasetOutputDefinitionService.save(idOrNull, projectId, connectionId, schema, table, outputColumns);
    }

    public DatasetOutputDefinition saveDatasetDefinition(
            UUID idOrNull,
            UUID projectId,
            UUID connectionId,
            String schema,
            String table,
            List<String> outputColumns,
            String definitionJson)
            throws Exception {
        return datasetOutputDefinitionService.save(
                idOrNull, projectId, connectionId, schema, table, outputColumns, definitionJson);
    }

    public List<ForeignKeyRef> listForeignKeys(UUID connectionId, String schema) throws Exception {
        return catalogMetadataService.listForeignKeys(connectionId, schema);
    }

    public void deleteDatasetDefinition(UUID id) {
        datasetOutputDefinitionService.delete(id);
    }

    public DataPage previewCsvSource(UUID sourceId, long offset, int limit) throws Exception {
        return csvIngestService.preview(sourceId, offset, limit);
    }

    public DataPage previewJoin(
            UUID connectionId,
            TableRef left,
            TableRef right,
            String leftKey,
            String rightKey,
            List<String> selectedColumns,
            long offset,
            int limit)
            throws Exception {
        return joinPreviewService.previewInnerJoin(
                connectionId, left, right, leftKey, rightKey, selectedColumns, offset, limit);
    }

    public DataPage previewJoin(
            UUID connectionId,
            TableRef left,
            TableRef right,
            String leftKey,
            String rightKey,
            JoinType joinType,
            List<String> selectedColumns,
            long offset,
            int limit)
            throws Exception {
        return joinPreviewService.previewJoin(
                connectionId, left, right, leftKey, rightKey, joinType, selectedColumns, offset, limit);
    }

    public DataPage previewDatasetGraph(UUID connectionId, DatasetGraphDefinition graph, long offset, int limit)
            throws Exception {
        return datasetGraphService.preview(connectionId, graph, offset, limit);
    }

    public String serializeDatasetGraph(DatasetGraphDefinition graph) throws Exception {
        return datasetGraphService.serialize(graph);
    }

    public DatasetGraphDefinition deserializeDatasetGraph(String json) throws Exception {
        return datasetGraphService.deserialize(json);
    }

    public TableExportResult exportTableToCsv(
            UUID connectionId, TableRef table, Path destination, Long maxRows) throws Exception {
        return tableExportService.exportTableToCsv(connectionId, table, destination, maxRows);
    }

    public long exportTableToCsv(UUID connectionId, TableRef table, Path destination) throws Exception {
        return tableExportService.exportTableToCsv(connectionId, table, destination, null).rowsWritten();
    }

    public Map<String, Map<String, Number>> cacheDiagnostics() {
        return cacheDiagnosticsService.snapshot();
    }

    public void invalidateAllCaches() {
        cacheInvalidationService.invalidateAll();
    }

    public void shutdown() {
        postgresConnectionManager.shutdown();
    }
}
