package com.dataweave.app.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.theme.DwSpacing
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ColumnMeta
import com.dataweave.backend.domain.ConnectionLifecycleState
import com.dataweave.backend.domain.ConnectionProfile
import com.dataweave.backend.domain.TableRef
import com.dataweave.backend.startup.StartupTiming
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/** Per-node fetch lifecycle for the lazy explorer tree. */
enum class ExplorerLoadState {
    Idle,
    Loading,
    Refreshing,
    Loaded,
    Empty,
    Error,
}

data class ExplorerSlice<T>(
    val state: ExplorerLoadState = ExplorerLoadState.Idle,
    val items: List<T> = emptyList(),
    val error: String? = null,
)

@Composable
fun DatabaseTreeSidebar(
    facade: ApplicationFacade,
    connectionId: UUID?,
    onSelectTable: (TableRef) -> Unit,
    onOpenConnections: () -> Unit,
    onConnectionEstablished: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf("") }
    var rootState by remember { mutableStateOf(ExplorerSlice<String>()) }
    var profile by remember { mutableStateOf<ConnectionProfile?>(null) }
    var connectionPhase by remember { mutableStateOf("Disconnected") }
    var lifecycleState by remember { mutableStateOf(ConnectionLifecycleState.DISCONNECTED) }
    val expandedDbs = remember { mutableStateMapOf<String, Boolean>() }
    val schemaSlices = remember { mutableStateMapOf<String, ExplorerSlice<String>>() }
    val tableSlices = remember { mutableStateMapOf<String, ExplorerSlice<TableRef>>() }
    val columnSlices = remember { mutableStateMapOf<String, ExplorerSlice<ColumnMeta>>() }
    val expandedSchemas = remember { mutableStateMapOf<String, Boolean>() }
    val expandedTables = remember { mutableStateMapOf<String, Boolean>() }
    var loadGeneration by remember { mutableStateOf(0) }
    var connectBusy by remember { mutableStateOf(false) }

    fun resetTree() {
        rootState = ExplorerSlice()
        schemaSlices.clear()
        tableSlices.clear()
        columnSlices.clear()
        expandedDbs.clear()
        expandedSchemas.clear()
        expandedTables.clear()
    }

    suspend fun timeoutMs(): Long {
        val seconds = profile?.connectTimeoutSeconds ?: 10
        return (seconds.coerceAtLeast(5) * 1000L).coerceAtMost(60_000L)
    }

    suspend fun <T> loadSlice(
        gen: Int,
        current: ExplorerSlice<T>,
        block: suspend () -> List<T>,
        onUpdate: (ExplorerSlice<T>) -> Unit,
    ) {
        val loadingState =
            if (current.state == ExplorerLoadState.Loaded || current.state == ExplorerLoadState.Empty) {
                ExplorerLoadState.Refreshing
            } else {
                ExplorerLoadState.Loading
            }
        onUpdate(current.copy(state = loadingState, error = null))
        try {
            val items = withContext(Dispatchers.IO) { withTimeout(timeoutMs()) { block() } }
            if (gen != loadGeneration) return
            onUpdate(
                if (items.isEmpty()) {
                    ExplorerSlice(state = ExplorerLoadState.Empty, items = emptyList())
                } else {
                    ExplorerSlice(state = ExplorerLoadState.Loaded, items = items)
                },
            )
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            if (gen != loadGeneration) return
            onUpdate(
                ExplorerSlice(
                    state = ExplorerLoadState.Error,
                    items = emptyList(),
                    error = ex.message ?: ex.javaClass.simpleName,
                ),
            )
        }
    }

    fun loadSchemas(gen: Int, database: String) {
        val id = connectionId ?: return
        val key = "db:$database"
        scope.launch {
            loadSlice(
                gen,
                schemaSlices[key] ?: ExplorerSlice(),
                { facade.listSchemas(id) },
            ) { schemaSlices[key] = it }
        }
    }

    fun loadTables(gen: Int, database: String, schema: String) {
        val id = connectionId ?: return
        val key = "db:$database:schema:$schema"
        scope.launch {
            loadSlice(
                gen,
                tableSlices[key] ?: ExplorerSlice(),
                { facade.listTables(id, schema) },
            ) { tableSlices[key] = it }
        }
    }

    fun loadColumns(gen: Int, database: String, schema: String, table: String) {
        val id = connectionId ?: return
        val key = "db:$database:schema:$schema:table:$table"
        scope.launch {
            loadSlice(
                gen,
                columnSlices[key] ?: ExplorerSlice(),
                { facade.listColumns(id, schema, table) },
            ) { columnSlices[key] = it }
        }
    }

    fun loadDatabases(gen: Int) {
        val id = connectionId ?: return
        scope.launch {
            loadSlice(gen, rootState, { facade.listDatabases(id) }) { slice ->
                rootState = slice
                if (slice.state == ExplorerLoadState.Loaded) {
                    val db = profile?.databaseName
                    if (db != null && db in slice.items) {
                        expandedDbs[db] = true
                        loadSchemas(gen, db)
                    }
                }
            }
        }
    }

    fun refreshAll() {
        val id = connectionId ?: return
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { facade.refreshSchema(id) } }
            val gen = loadGeneration + 1
            loadGeneration = gen
            resetTree()
            loadDatabases(gen)
        }
    }

    fun connectActiveProfile() {
        val id = connectionId ?: return
        connectBusy = true
        scope.launch {
            try {
                val st = withContext(Dispatchers.IO) { facade.connectConnection(id) }
                lifecycleState = st.state()
                connectionPhase = "${st.state()}: ${st.message()}"
                if (st.state() == ConnectionLifecycleState.CONNECTED) {
                    onConnectionEstablished(id)
                    refreshAll()
                } else if (st.state() == ConnectionLifecycleState.ERROR) {
                    rootState = ExplorerSlice(state = ExplorerLoadState.Error, error = st.message())
                }
            } catch (ex: Exception) {
                rootState =
                    ExplorerSlice(
                        state = ExplorerLoadState.Error,
                        error = ex.message ?: "Connect failed",
                    )
            } finally {
                connectBusy = false
            }
        }
    }

    LaunchedEffect(connectionId) {
        resetTree()
        val id = connectionId
        if (id == null) {
            connectionPhase = "Disconnected"
            lifecycleState = ConnectionLifecycleState.DISCONNECTED
            profile = null
            return@LaunchedEffect
        }
        val gen = loadGeneration + 1
        loadGeneration = gen
        profile = withContext(Dispatchers.IO) { facade.listConnections().firstOrNull { it.id == id } }
        val status = withContext(Dispatchers.IO) { facade.connectionStatus(id) }
        lifecycleState = status.state()
        connectionPhase = "${status.state()}: ${status.message()}"
        when (status.state()) {
            ConnectionLifecycleState.CONNECTED -> {
                withContext(Dispatchers.IO) { facade.refreshSchema(id) }
                loadDatabases(gen)
            }
            ConnectionLifecycleState.CONNECTING, ConnectionLifecycleState.RECONNECTING ->
                connectionPhase = "${status.state()}…"
            ConnectionLifecycleState.ERROR ->
                rootState =
                    ExplorerSlice(
                        state = ExplorerLoadState.Error,
                        error = status.message(),
                    )
            ConnectionLifecycleState.DISCONNECTED ->
                rootState = ExplorerSlice(state = ExplorerLoadState.Idle)
            else ->
                rootState =
                    ExplorerSlice(
                        state = ExplorerLoadState.Error,
                        error = status.message().ifBlank { "Connect to load metadata" },
                    )
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(DwSpacing.sm),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Explorer", style = MaterialTheme.typography.titleSmall)
            }
            IconButton(onClick = { refreshAll() }, enabled = connectionId != null) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh metadata")
            }
        }
        Text(
            connectionPhase,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val springMs = StartupTiming.springReadyMs()
        if (springMs > 0) {
            Text(
                "Backend ready ${springMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        val showObjectFilter =
            connectionId != null &&
                (rootState.state == ExplorerLoadState.Loaded ||
                    rootState.state == ExplorerLoadState.Empty ||
                    rootState.state == ExplorerLoadState.Refreshing ||
                    (rootState.state == ExplorerLoadState.Loading && rootState.items.isNotEmpty()))
        if (showObjectFilter) {
            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = DwSpacing.sm),
                singleLine = true,
                label = { Text("Filter objects") },
                placeholder = { Text("Schema, table, column…") },
            )
        }
        when {
            connectionId == null ->
                DisconnectedExplorerPanel(onOpenConnections = onOpenConnections)
            lifecycleState == ConnectionLifecycleState.DISCONNECTED ->
                DisconnectedProfilePanel(
                    profileName = profile?.name ?: "Profile",
                    database = profile?.databaseName,
                    busy = connectBusy,
                    onConnect = { connectActiveProfile() },
                    onManage = onOpenConnections,
                )
            rootState.state == ExplorerLoadState.Loading || rootState.state == ExplorerLoadState.Refreshing ->
                Row(Modifier.padding(DwSpacing.sm), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Loading databases…")
                }
            rootState.state == ExplorerLoadState.Error ->
                Column(Modifier.padding(DwSpacing.sm)) {
                    ErrorRow(
                        rootState.error ?: "Error",
                        onRetry =
                            if (lifecycleState == ConnectionLifecycleState.CONNECTED) {
                                { refreshAll() }
                            } else {
                                { connectActiveProfile() }
                            },
                    )
                    if (lifecycleState != ConnectionLifecycleState.CONNECTED) {
                        OutlinedButton(onClick = onOpenConnections, modifier = Modifier.padding(top = 4.dp)) {
                            Text("Edit connection")
                        }
                    }
                }
            else ->
                LazyColumn(Modifier.weight(1f)) {
                    val dbs =
                        rootState.items.filter { filter.isBlank() || it.contains(filter, ignoreCase = true) }
                    items(dbs, key = { it }) { db ->
                        val dbExpanded = expandedDbs[db] == true
                        val isActiveDb = db == profile?.databaseName
                        TreeRow(
                            label = db,
                            prefix = if (dbExpanded) "▼" else "▶",
                            indent = 0,
                            emphasized = isActiveDb,
                            onClick = {
                                expandedDbs[db] = !dbExpanded
                                if (!dbExpanded) {
                                    if (isActiveDb) {
                                        loadSchemas(loadGeneration, db)
                                    }
                                }
                            },
                        )
                        if (!isActiveDb && dbExpanded) {
                            Row(Modifier.padding(start = 24.dp, bottom = 4.dp)) {
                                TextButton(
                                    onClick = {
                                        val id = connectionId ?: return@TextButton
                                        scope.launch {
                                            val st =
                                                withContext(Dispatchers.IO) {
                                                    facade.switchConnectionDatabase(id, db)
                                                }
                                            connectionPhase = "${st.state()}: ${st.message()}"
                                            if (st.state() == ConnectionLifecycleState.CONNECTED) {
                                                profile = facade.listConnections().firstOrNull { it.id == id }
                                                refreshAll()
                                            }
                                        }
                                    },
                                ) { Text("Switch to $db") }
                            }
                        }
                        if (dbExpanded && isActiveDb) {
                            DatabaseSchemasSection(
                                database = db,
                                filter = filter,
                                schemaSlice = schemaSlices["db:$db"] ?: ExplorerSlice(),
                                tableSlices = tableSlices,
                                columnSlices = columnSlices,
                                expandedSchemas = expandedSchemas,
                                expandedTables = expandedTables,
                                onExpandSchema = { schema, expand ->
                                    expandedSchemas[schema] = expand
                                    if (expand) loadTables(loadGeneration, db, schema)
                                },
                                onExpandTable = { schema, table, expand ->
                                    val tKey = "$schema.$table"
                                    expandedTables[tKey] = expand
                                    if (expand) loadColumns(loadGeneration, db, schema, table)
                                },
                                onSelectTable = onSelectTable,
                                onRetrySchemas = { loadSchemas(loadGeneration, db) },
                                onRetryTables = { schema -> loadTables(loadGeneration, db, schema) },
                                onRetryColumns = { schema, table -> loadColumns(loadGeneration, db, schema, table) },
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun DatabaseSchemasSection(
    database: String,
    filter: String,
    schemaSlice: ExplorerSlice<String>,
    tableSlices: MutableMap<String, ExplorerSlice<TableRef>>,
    columnSlices: MutableMap<String, ExplorerSlice<ColumnMeta>>,
    expandedSchemas: MutableMap<String, Boolean>,
    expandedTables: MutableMap<String, Boolean>,
    onExpandSchema: (String, Boolean) -> Unit,
    onExpandTable: (String, String, Boolean) -> Unit,
    onSelectTable: (TableRef) -> Unit,
    onRetrySchemas: () -> Unit,
    onRetryTables: (String) -> Unit,
    onRetryColumns: (String, String) -> Unit,
) {
    when (schemaSlice.state) {
        ExplorerLoadState.Loading, ExplorerLoadState.Refreshing ->
            Text("Loading schemas…", modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        ExplorerLoadState.Error ->
            ErrorRow(schemaSlice.error ?: "Schema error", onRetry = onRetrySchemas, indent = 16)
        ExplorerLoadState.Empty -> Text("(no schemas)", modifier = Modifier.padding(start = 16.dp))
        ExplorerLoadState.Loaded, ExplorerLoadState.Idle -> {
            val schemas =
                schemaSlice.items.filter { filter.isBlank() || it.contains(filter, ignoreCase = true) }
            schemas.forEach { schema ->
                val expanded = expandedSchemas[schema] == true
                TreeRow(
                    label = schema,
                    prefix = if (expanded) "▼" else "▶",
                    indent = 1,
                    onClick = { onExpandSchema(schema, !expanded) },
                )
                if (expanded) {
                    val tKey = "db:$database:schema:$schema"
                    val tableSlice = tableSlices[tKey] ?: ExplorerSlice()
                    when (tableSlice.state) {
                        ExplorerLoadState.Loading, ExplorerLoadState.Refreshing ->
                            Text("Loading tables…", modifier = Modifier.padding(start = 32.dp))
                        ExplorerLoadState.Error ->
                            ErrorRow(tableSlice.error ?: "Tables error", onRetry = { onRetryTables(schema) }, indent = 32)
                        ExplorerLoadState.Empty -> Text("(no tables)", modifier = Modifier.padding(start = 32.dp))
                        ExplorerLoadState.Loaded, ExplorerLoadState.Idle -> {
                            val tables =
                                tableSlice.items.filter {
                                    filter.isBlank() ||
                                        it.name.contains(filter, ignoreCase = true) ||
                                        it.schema.contains(filter, ignoreCase = true)
                                }
                            tables.forEach { table ->
                                val tableKey = "${schema}.${table.name}"
                                val tableExpanded = expandedTables[tableKey] == true
                                TreeRow(
                                    label = table.name,
                                    prefix = if (tableExpanded) "▼" else "▶",
                                    indent = 2,
                                    onClick = {
                                        onSelectTable(table)
                                        onExpandTable(schema, table.name, !tableExpanded)
                                    },
                                )
                                if (tableExpanded) {
                                    val cKey = "db:$database:schema:$schema:table:${table.name}"
                                    val colSlice = columnSlices[cKey] ?: ExplorerSlice()
                                    when (colSlice.state) {
                                        ExplorerLoadState.Loading, ExplorerLoadState.Refreshing ->
                                            Text("Loading columns…", modifier = Modifier.padding(start = 48.dp))
                                        ExplorerLoadState.Error ->
                                            ErrorRow(
                                                colSlice.error ?: "Columns error",
                                                onRetry = { onRetryColumns(schema, table.name) },
                                                indent = 48,
                                            )
                                        ExplorerLoadState.Empty ->
                                            Text("(no columns)", modifier = Modifier.padding(start = 48.dp))
                                        ExplorerLoadState.Loaded, ExplorerLoadState.Idle ->
                                            colSlice.items
                                                .filter { filter.isBlank() || it.name.contains(filter, ignoreCase = true) }
                                                .forEach { col ->
                                                    Text(
                                                        "${col.name} : ${col.dataType}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        modifier = Modifier.padding(start = 48.dp, top = 2.dp, bottom = 2.dp),
                                                    )
                                                }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeRow(
    label: String,
    prefix: String,
    indent: Int,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    Text(
        "$prefix $label",
        style = if (emphasized) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
        color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = (8 + indent * 16).dp, top = 4.dp, bottom = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun DisconnectedExplorerPanel(onOpenConnections: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(DwSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "No connection selected",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Choose a saved profile on Home and press Connect, or create a new connection.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onOpenConnections, modifier = Modifier.fillMaxWidth()) {
            Text("Connections")
        }
    }
}

@Composable
private fun DisconnectedProfilePanel(
    profileName: String,
    database: String?,
    busy: Boolean,
    onConnect: () -> Unit,
    onManage: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(DwSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(profileName, style = MaterialTheme.typography.titleSmall)
        database?.let {
            Text("Database: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "Profile saved — connect to load schemas and tables.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onConnect, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (busy) "Connecting…" else "Connect")
        }
        OutlinedButton(onClick = onManage, modifier = Modifier.fillMaxWidth()) {
            Text("Manage connection")
        }
    }
}

@Composable
private fun ErrorRow(message: String, onRetry: () -> Unit, indent: Int = 8) {
    Row(
        Modifier.fillMaxWidth().padding(start = indent.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
