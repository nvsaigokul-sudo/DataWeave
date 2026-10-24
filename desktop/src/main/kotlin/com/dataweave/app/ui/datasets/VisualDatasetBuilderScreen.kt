package com.dataweave.app.ui.datasets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.common.ResultDataCard
import com.dataweave.app.ui.workspace.WorkspaceContext
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.DatasetGraphDefinition
import com.dataweave.backend.domain.DatasetOutputDefinition
import com.dataweave.backend.domain.ForeignKeyRef
import com.dataweave.backend.domain.JoinType
import com.dataweave.backend.domain.TableRef
import com.dataweave.backend.domain.WorkspaceProject
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Path
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VisualDatasetBuilderScreen(
    facade: ApplicationFacade,
    workspace: WorkspaceContext,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var projects by remember { mutableStateOf<List<WorkspaceProject>>(emptyList()) }
    var projectId by remember { mutableStateOf<UUID?>(null) }
    var connectionId by remember { mutableStateOf(workspace.activeConnectionId) }
    var schema by remember { mutableStateOf(workspace.selectedSchema) }
    var tables by remember { mutableStateOf<List<TableRef>>(emptyList()) }
    var columnsByTable by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var fkSuggestions by remember { mutableStateOf<List<ForeignKeyRef>>(emptyList()) }
    var savedDefs by remember { mutableStateOf<List<DatasetOutputDefinition>>(emptyList()) }
    var editingId by remember { mutableStateOf<UUID?>(null) }
    var sourceTables by remember { mutableStateOf<List<DatasetGraphDefinition.DatasetSourceTable>>(emptyList()) }
    var joins by remember { mutableStateOf<List<DatasetGraphDefinition.DatasetJoinEdge>>(emptyList()) }
    var outputCols by remember { mutableStateOf<List<DatasetGraphDefinition.DatasetOutputColumn>>(emptyList()) }
    var filterText by remember { mutableStateOf("") }
    var filterColumn by remember { mutableStateOf("") }
    var sortColumn by remember { mutableStateOf("") }
    var sortDesc by remember { mutableStateOf(false) }
    var generatedSql by remember { mutableStateOf("") }
    var previewPage by remember { mutableStateOf<com.dataweave.backend.domain.DataPage?>(null) }
    var previewStale by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("") }

    fun graph(): DatasetGraphDefinition =
        DatasetGraphDefinition(
            1,
            schema,
            sourceTables,
            joins,
            outputCols,
            filterText.ifBlank { null },
            filterColumn.ifBlank { null },
            sortColumn.ifBlank { null },
            sortDesc,
        )

    fun markStale() {
        previewStale = true
        previewPage = null
    }

    LaunchedEffect(Unit) {
        projects = withContext(Dispatchers.IO) { facade.listProjects() }
        if (projectId == null && projects.isNotEmpty()) projectId = projects.first().id
    }

    LaunchedEffect(workspace.activeConnectionId, workspace.activeDatabase) {
        connectionId = workspace.activeConnectionId
        workspace.activeDatabase?.let { /* schema stays */ }
    }

    LaunchedEffect(workspace.selectedSchema) {
        if (workspace.selectedSchema.isNotBlank()) schema = workspace.selectedSchema
    }

    LaunchedEffect(connectionId, schema) {
        val id = connectionId ?: return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                facade.refreshSchema(id)
                facade.listTables(id, schema)
            }
        }.onSuccess { tables = it }
    }

    LaunchedEffect(projectId) {
        val pid = projectId ?: return@LaunchedEffect
        savedDefs = withContext(Dispatchers.IO) { facade.listDatasetDefinitions(pid) }
    }

    LaunchedEffect(connectionId, schema) {
        val id = connectionId ?: return@LaunchedEffect
        runCatching { withContext(Dispatchers.IO) { facade.listForeignKeys(id, schema) } }
            .onSuccess { fkSuggestions = it }
    }

    fun loadColumns(table: String) {
        val id = connectionId ?: return
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { facade.listColumns(id, schema, table).map { it.name } }
            }.onSuccess { columnsByTable = columnsByTable + (table to it) }
        }
    }

    fun restoreGraph(def: DatasetOutputDefinition) {
        editingId = def.id
        val json = def.definitionJson
        if (json.isNullOrBlank()) {
            error = "Definition has no saved graph JSON"
            return
        }
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { facade.deserializeDatasetGraph(json) } }
                .onSuccess { g ->
                    schema = g.schema()
                    sourceTables = g.sourceTables()
                    joins = g.joins()
                    outputCols = g.outputColumns()
                    filterText = g.filterText() ?: ""
                    filterColumn = g.filterColumn() ?: ""
                    sortColumn = g.sortColumn() ?: ""
                    sortDesc = g.sortDescending()
                    markStale()
                    status = "Loaded ${def.id}"
                }
                .onFailure { error = it.message }
        }
    }

    Row(modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.width(220.dp).fillMaxHeight().verticalScroll(rememberScrollState())) {
            Text("Projects", style = MaterialTheme.typography.titleSmall)
            projects.forEach { p ->
                OutlinedButton(onClick = { projectId = p.id }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (projectId == p.id) "[${p.name}]" else p.name)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Tables ($schema)", style = MaterialTheme.typography.titleSmall)
            tables.forEach { t ->
                Button(
                    onClick = {
                        loadColumns(t.name)
                        val alias = nextAlias(sourceTables, t.name)
                        sourceTables = sourceTables + DatasetGraphDefinition.DatasetSourceTable(alias, t.name, 40.0, 40.0 + sourceTables.size * 90)
                        markStale()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("+ ${t.name}") }
                columnsByTable[t.name]?.forEach { col ->
                    OutlinedButton(
                        onClick = {
                            val alias = sourceTables.find { it.tableName() == t.name }?.alias() ?: return@OutlinedButton
                            val key = "$alias.$col"
                            if (outputCols.none { it.sourceAlias() == alias && it.columnName() == col }) {
                                outputCols = outputCols + DatasetGraphDefinition.DatasetOutputColumn(key, alias, col, col)
                                markStale()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("→ $col") }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("FK suggestions", style = MaterialTheme.typography.labelSmall)
            fkSuggestions.take(8).forEach { fk ->
                OutlinedButton(
                    onClick = {
                        val left = sourceTables.find { it.tableName() == fk.table() }
                        val right = sourceTables.find { it.tableName() == fk.referencedTable() }
                        if (left == null || right == null) {
                            error = "Add both ${fk.table()} and ${fk.referencedTable()} to canvas first"
                            return@OutlinedButton
                        }
                        joins =
                            joins +
                                DatasetGraphDefinition.DatasetJoinEdge(
                                    left.alias(),
                                    fk.column(),
                                    JoinType.INNER,
                                    right.alias(),
                                    fk.referencedColumn(),
                                )
                        markStale()
                        status = "Join added (confirm keys)"
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("${fk.table()}.${fk.column()} → ${fk.referencedTable()}.${fk.referencedColumn()}", maxLines = 2)
                }
            }
        }

        Column(Modifier.weight(0.55f).fillMaxHeight()) {
            Text("Canvas", style = MaterialTheme.typography.titleSmall)
            if (previewStale) {
                Text("Preview stale — run Preview after changes", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                sourceTables.forEach { st ->
                    var dragOffset by remember(st.alias()) { mutableStateOf(IntOffset(st.canvasX().roundToInt(), st.canvasY().roundToInt())) }
                    Card(
                        Modifier
                            .offset { dragOffset }
                            .width(160.dp)
                            .pointerInput(st.alias()) {
                                detectDragGestures { change, drag ->
                                    change.consume()
                                    dragOffset = IntOffset(dragOffset.x + drag.x.roundToInt(), dragOffset.y + drag.y.roundToInt())
                                    sourceTables =
                                        sourceTables.map {
                                            if (it.alias() == st.alias()) {
                                                DatasetGraphDefinition.DatasetSourceTable(
                                                    it.alias(),
                                                    it.tableName(),
                                                    dragOffset.x.toDouble(),
                                                    dragOffset.y.toDouble(),
                                                )
                                            } else {
                                                it
                                            }
                                        }
                                    markStale()
                                }
                            },
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(st.tableName(), style = MaterialTheme.typography.titleSmall)
                            Text("alias: ${st.alias()}", style = MaterialTheme.typography.labelSmall)
                            OutlinedButton(onClick = {
                                sourceTables = sourceTables.filter { it.alias() != st.alias() }
                                joins = joins.filter { it.leftAlias() != st.alias() && it.rightAlias() != st.alias() }
                                outputCols = outputCols.filter { it.sourceAlias() != st.alias() }
                                markStale()
                            }) { Text("Remove") }
                        }
                    }
                }
            }
            Text("Joins", style = MaterialTheme.typography.labelMedium)
            joins.forEachIndexed { i, j ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${j.joinType()}: ${j.leftAlias()}.${j.leftColumn()} = ${j.rightAlias()}.${j.rightColumn()}")
                    OutlinedButton(onClick = { joins = joins.filterIndexed { idx, _ -> idx != i }; markStale() }) { Text("×") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JoinType.entries.forEach { jt ->
                    OutlinedButton(
                        onClick = {
                            if (sourceTables.size < 2) {
                                error = "Need two tables on canvas"
                                return@OutlinedButton
                            }
                            val left = sourceTables[0]
                            val right = sourceTables[1]
                            joins =
                                joins +
                                    DatasetGraphDefinition.DatasetJoinEdge(
                                        left.alias(),
                                        "id",
                                        jt,
                                        right.alias(),
                                        "id",
                                    )
                            markStale()
                        },
                    ) { Text(jt.name) }
                }
            }
        }

        Column(Modifier.weight(0.45f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            Text("Output columns", style = MaterialTheme.typography.titleSmall)
            outputCols.forEachIndexed { i, col ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        col.outputAlias() ?: col.columnName(),
                        {
                            outputCols =
                                outputCols.mapIndexed { idx, c ->
                                    if (idx == i) {
                                        DatasetGraphDefinition.DatasetOutputColumn(
                                            c.alias(),
                                            c.sourceAlias(),
                                            c.columnName(),
                                            it,
                                        )
                                    } else {
                                        c
                                    }
                                }
                            markStale()
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("${col.sourceAlias()}.${col.columnName()}") },
                    )
                    OutlinedButton(onClick = {
                        if (i > 0) {
                            val m = outputCols.toMutableList()
                            m[i - 1] = outputCols[i].also { m[i] = outputCols[i - 1] }
                            outputCols = m
                            markStale()
                        }
                    }) { Text("↑") }
                    OutlinedButton(onClick = {
                        if (i < outputCols.lastIndex) {
                            val m = outputCols.toMutableList()
                            m[i + 1] = outputCols[i].also { m[i] = outputCols[i + 1] }
                            outputCols = m
                            markStale()
                        }
                    }) { Text("↓") }
                    OutlinedButton(onClick = { outputCols = outputCols.filterIndexed { idx, _ -> idx != i }; markStale() }) { Text("Remove") }
                }
            }
            OutlinedTextField(filterColumn, { filterColumn = it; markStale() }, label = { Text("Filter column (alias.col)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(filterText, { filterText = it; markStale() }, label = { Text("Filter value") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sortColumn, { sortColumn = it; markStale() }, label = { Text("Sort column") }, modifier = Modifier.fillMaxWidth())
            Row {
                OutlinedButton(onClick = { sortDesc = !sortDesc; markStale() }) {
                    Text(if (sortDesc) "Sort DESC" else "Sort ASC")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                Button(
                    enabled = connectionId != null && sourceTables.isNotEmpty(),
                    onClick = {
                        val id = connectionId ?: return@Button
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    generatedSql = facade.generateDatasetGraphSql(graph(), 100, 0)
                                    facade.previewDatasetGraph(id, graph(), 0, 100)
                                }
                            }.onSuccess {
                                previewPage = it
                                previewStale = false
                                error = null
                                status = "Preview OK"
                            }.onFailure {
                                error = it.message
                            }
                        }
                    },
                ) { Text("Preview") }
                Button(
                    enabled = projectId != null && connectionId != null,
                    onClick = {
                        val pid = projectId ?: return@Button
                        val cid = connectionId ?: return@Button
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    val json = facade.serializeDatasetGraph(graph())
                                    generatedSql = facade.generateDatasetGraphSql(graph(), 100, 0)
                                    facade.saveDatasetDefinition(
                                        editingId,
                                        pid,
                                        cid,
                                        schema,
                                        sourceTables.firstOrNull()?.tableName() ?: "graph",
                                        outputCols.map { it.outputAlias() ?: it.columnName() },
                                        json,
                                    )
                                }
                            }.onSuccess {
                                editingId = it.id
                                savedDefs = withContext(Dispatchers.IO) { facade.listDatasetDefinitions(pid) }
                                status = "Saved ${it.id}"
                            }.onFailure { error = it.message }
                        }
                    },
                ) { Text("Save") }
                OutlinedButton(
                    onClick = {
                        val dialog = FileDialog(null as Frame?, "Export preview CSV", FileDialog.SAVE)
                        dialog.file = "dataset.csv"
                        dialog.isVisible = true
                        val dir = dialog.directory ?: return@OutlinedButton
                        val file = dialog.file ?: return@OutlinedButton
                        val page = previewPage ?: return@OutlinedButton
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    java.nio.file.Files.writeString(
                                        Path.of(dir, file),
                                        buildString {
                                            append(page.columns.joinToString(",") { it.name })
                                            append("\n")
                                            page.rows.forEach { row ->
                                                append(page.columns.joinToString(",") { row[it.name]?.toString() ?: "" })
                                                append("\n")
                                            }
                                        },
                                    )
                                }
                            }.onSuccess { status = "Exported CSV" }
                                .onFailure { error = it.message }
                        }
                    },
                ) { Text("Export") }
            }
            if (generatedSql.isNotBlank()) {
                Text("Generated SQL", style = MaterialTheme.typography.labelMedium)
                Text(generatedSql, style = MaterialTheme.typography.bodySmall, modifier = Modifier.height(80.dp).verticalScroll(rememberScrollState()))
            }
            Text("Saved definitions", style = MaterialTheme.typography.labelMedium)
            savedDefs.forEach { def ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { restoreGraph(def) }) { Text(def.sourceTable) }
                    OutlinedButton(onClick = {
                        scope.launch {
                            runCatching { withContext(Dispatchers.IO) { facade.deleteDatasetDefinition(def.id) } }
                                .onSuccess {
                                    savedDefs = withContext(Dispatchers.IO) { facade.listDatasetDefinitions(projectId!!) }
                                    if (editingId == def.id) editingId = null
                                }
                        }
                    }) { Text("Del") }
                    OutlinedButton(onClick = {
                        scope.launch {
                            runCatching { withContext(Dispatchers.IO) { facade.duplicateDatasetDefinition(def.id) } }
                                .onSuccess {
                                    savedDefs = withContext(Dispatchers.IO) { facade.listDatasetDefinitions(projectId!!) }
                                }
                        }
                    }) { Text("Dup") }
                }
            }
            Text(status, style = MaterialTheme.typography.labelSmall)
            ResultDataCard(previewPage, error, Modifier.fillMaxWidth())
        }
    }
}

private fun nextAlias(existing: List<DatasetGraphDefinition.DatasetSourceTable>, tableName: String): String {
    val base = tableName.take(3).lowercase().ifBlank { "t" }
    var n = 1
    var candidate = base
    while (existing.any { it.alias() == candidate }) {
        n++
        candidate = base + n
    }
    return candidate
}
