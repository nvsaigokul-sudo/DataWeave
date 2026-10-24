package com.dataweave.app.ui.datasets

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.common.ResultDataCard
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionProfile
import com.dataweave.backend.domain.CsvSource
import com.dataweave.backend.domain.DataPage
import com.dataweave.backend.domain.TableRef
import com.dataweave.backend.domain.WorkspaceProject
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Path
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DatasetBuilderScreen(
    facade: ApplicationFacade,
    activeConnectionId: UUID?,
    modifier: Modifier = Modifier,
) {
    var projects by remember { mutableStateOf<List<WorkspaceProject>>(emptyList()) }
    var projectId by remember { mutableStateOf<UUID?>(null) }
    var connections by remember { mutableStateOf<List<ConnectionProfile>>(emptyList()) }
    var connectionId by remember { mutableStateOf(activeConnectionId) }
    var schemas by remember { mutableStateOf<List<String>>(emptyList()) }
    var schema by remember { mutableStateOf("public") }
    var tables by remember { mutableStateOf<List<TableRef>>(emptyList()) }
    var table by remember { mutableStateOf<TableRef?>(null) }
    var columnFilter by remember { mutableStateOf("") }
    var csvSources by remember { mutableStateOf<List<CsvSource>>(emptyList()) }
    var csvPath by remember { mutableStateOf("") }
    var csvName by remember { mutableStateOf("import") }
    var leftTable by remember { mutableStateOf("") }
    var rightTable by remember { mutableStateOf("") }
    var leftKey by remember { mutableStateOf("id") }
    var rightKey by remember { mutableStateOf("id") }
    var page by remember { mutableStateOf<DataPage?>(null) }
    var sourceColumns by remember { mutableStateOf<List<String>>(emptyList()) }
    var outputColumns by remember { mutableStateOf<List<String>>(emptyList()) }
    var outputRenames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val previewScope = remember { "dataset-ui" }

    LaunchedEffect(Unit) {
        projects = withContext(Dispatchers.IO) { facade.listProjects() }
        if (projectId == null && projects.isNotEmpty()) projectId = projects.first().id
        connections = withContext(Dispatchers.IO) { facade.listConnections() }
        if (connectionId == null && connections.isNotEmpty()) connectionId = connections.first().id
    }

    LaunchedEffect(projectId) {
        val pid = projectId ?: return@LaunchedEffect
        csvSources = withContext(Dispatchers.IO) { facade.listCsvSources(pid) }
    }

    LaunchedEffect(connectionId, schema) {
        val id = connectionId ?: return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                facade.refreshSchema(id)
                facade.listSchemas(id)
            }
        }.onSuccess { schemas = it }
        runCatching { withContext(Dispatchers.IO) { facade.listTables(id, schema) } }
            .onSuccess { tables = it }
    }

    Column(modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Dataset builder — map columns, CSV register/import, join preview")
        Text(
            "Register CSV stores a path reference; Preview reads file contents without importing into PostgreSQL.",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            projects.forEach { p ->
                Button(onClick = { projectId = p.id }) {
                    Text(if (projectId == p.id) "[${p.name}]" else p.name)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            connections.forEach { c ->
                Button(onClick = { connectionId = c.id }) {
                    Text(if (connectionId == c.id) "[${c.name}]" else c.name)
                }
            }
        }

        Text("Table column preview")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tables.take(8).forEach { t ->
                Button(onClick = { table = t }) {
                    Text(if (table == t) "[${t.name}]" else t.name)
                }
            }
        }
        OutlinedTextField(
            value = columnFilter,
            onValueChange = { columnFilter = it },
            label = { Text("Columns (comma-separated, empty = all)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            enabled = connectionId != null && table != null,
            onClick = {
                val id = connectionId ?: return@Button
                val t = table ?: return@Button
                val cols =
                    columnFilter.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                scope.launch {
                    error = null
                    runCatching {
                        withContext(Dispatchers.IO) {
                            facade.beginDatasetPreview(previewScope)
                            facade
                                .previewDataset(previewScope, id, t, cols, 0, 100)
                                .get()
                        }
                    }.onSuccess {
                        page = it
                        sourceColumns = it.columns.map { c -> c.name }
                        if (outputColumns.isEmpty()) outputColumns = sourceColumns
                        status = "Column preview OK"
                    }.onFailure {
                        page = null
                        error = it.message
                    }
                }
            },
        ) {
            Text("Preview columns")
        }

        Text("Output columns (drag to reorder, or ↑/↓; double-click rename via field)")
        LazyColumn(Modifier.weight(0.15f)) {
            itemsIndexed(outputColumns) { index, col ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier =
                        Modifier.pointerInput(index) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragIndex = index },
                                onDragEnd = { dragIndex = null },
                                onDragCancel = { dragIndex = null },
                                onDrag = { _, _ -> },
                            )
                        },
                ) {
                    Text(if (dragIndex == index) "⋮⋮" else "▣", modifier = Modifier.padding(end = 4.dp))
                    OutlinedTextField(
                        outputRenames[col] ?: col,
                        { outputRenames = outputRenames + (col to it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Output name") },
                    )
                    Button(onClick = { if (index > 0) outputColumns = outputColumns.swap(index, index - 1) }) { Text("↑") }
                    Button(onClick = {
                        if (index < outputColumns.lastIndex) outputColumns = outputColumns.swap(index, index + 1)
                    }) { Text("↓") }
                    Button(onClick = { outputColumns = outputColumns - col }) { Text("Remove") }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sourceColumns.forEach { col ->
                if (col !in outputColumns) {
                    Button(onClick = { outputColumns = outputColumns + col }) { Text("+ $col") }
                }
            }
            Button(
                enabled = projectId != null && connectionId != null && table != null && outputColumns.isNotEmpty(),
                onClick = {
                    val pid = projectId ?: return@Button
                    val id = connectionId ?: return@Button
                    val t = table ?: return@Button
                    val names = outputColumns.map { outputRenames[it] ?: it }
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                facade.saveDatasetDefinition(null, pid, id, t.schema, t.name, names)
                            }
                        }.onSuccess { status = "Saved dataset definition ${it.id}" }
                            .onFailure { error = it.message }
                    }
                },
            ) { Text("Save output definition") }
        }

        Text("CSV register (path-based, not DB import)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(csvName, { csvName = it }, label = { Text("Name") })
            OutlinedTextField(csvPath, { csvPath = it }, modifier = Modifier.weight(1f), label = { Text("Path") })
            Button(onClick = {
                val dialog = FileDialog(null as Frame?, "Choose CSV", FileDialog.LOAD)
                dialog.isVisible = true
                val dir = dialog.directory
                val file = dialog.file
                if (dir != null && file != null) {
                    csvPath = File(dir, file).absolutePath
                }
            }) {
                Text("Browse…")
            }
            Button(
                enabled = projectId != null && csvPath.isNotBlank(),
                onClick = {
                    val pid = projectId ?: return@Button
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                facade.registerCsvSource(pid, csvName, Path.of(csvPath))
                            }
                        }.onSuccess {
                            csvSources = withContext(Dispatchers.IO) { facade.listCsvSources(pid) }
                            status = "Registered CSV ${it.name}"
                        }.onFailure {
                            error = it.message
                        }
                    }
                },
            ) {
                Text("Register CSV")
            }
        }
        LazyColumn(Modifier.weight(0.2f)) {
            items(csvSources) { source ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${source.name} (${source.filePath})")
                    Button(onClick = {
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) { facade.previewCsvSource(source.id, 0, 50) }
                            }.onSuccess {
                                page = it
                                status = "CSV preview"
                            }.onFailure {
                                error = it.message
                            }
                        }
                    }) {
                        Text("Preview")
                    }
                }
            }
        }

        Text("Inner join preview")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(leftTable, { leftTable = it }, label = { Text("Left table") })
            OutlinedTextField(rightTable, { rightTable = it }, label = { Text("Right table") })
            OutlinedTextField(leftKey, { leftKey = it }, label = { Text("Left key") })
            OutlinedTextField(rightKey, { rightKey = it }, label = { Text("Right key") })
        }
        Button(
            enabled = connectionId != null && leftTable.isNotBlank() && rightTable.isNotBlank(),
            onClick = {
                val id = connectionId ?: return@Button
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            facade.previewJoin(
                                id,
                                TableRef(schema, leftTable),
                                TableRef(schema, rightTable),
                                leftKey,
                                rightKey,
                                listOf("left.$leftKey", "right.$rightKey"),
                                0,
                                100,
                            )
                        }
                    }.onSuccess {
                        page = it
                        status = "Join preview OK"
                    }.onFailure {
                        error = it.message
                    }
                }
            },
        ) {
            Text("Preview join")
        }

        Text(status)
        ResultDataCard(page, error, Modifier.weight(0.35f))
    }
}

private fun <T> List<T>.swap(a: Int, b: Int): List<T> {
    val m = toMutableList()
    val tmp = m[a]
    m[a] = m[b]
    m[b] = tmp
    return m
}
