package com.dataweave.app.ui.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ColumnMeta
import com.dataweave.backend.domain.DataPage
import com.dataweave.backend.domain.TableBrowseOptions
import com.dataweave.backend.domain.TableRef
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.file.Path
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpreadsheetDataGrid(
    facade: ApplicationFacade,
    connectionId: UUID,
    table: TableRef,
    modifier: Modifier = Modifier,
) {
    var page by remember { mutableStateOf<DataPage?>(null) }
    var offset by remember { mutableStateOf(0L) }
    var pageSize by remember { mutableIntStateOf(100) }
    var loading by remember { mutableStateOf(false) }
    var loadJob by remember { mutableStateOf<Job?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var globalFilter by remember { mutableStateOf("") }
    var columnFilter by remember { mutableStateOf<String?>(null) }
    var columnFilterText by remember { mutableStateOf("") }
    var sortColumn by remember { mutableStateOf<String?>(null) }
    var sortDesc by remember { mutableStateOf(false) }
    var lastLoadMs by remember { mutableStateOf<Long?>(null) }
    var hiddenColumns by remember { mutableStateOf(setOf<String>()) }
    var columnOrder by remember { mutableStateOf<List<String>>(emptyList()) }
    var columnWidths by remember { mutableStateOf(mapOf<String, Int>()) }
    var columnMenuFor by remember { mutableStateOf<String?>(null) }
    var longValue by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scopeKey = remember(table) { "sheet-${table.schema}.${table.name}" }
    val hScrollBody = rememberScrollState()
    val hScrollHeader = rememberScrollState()

    fun visibleColumns(meta: List<ColumnMeta>): List<ColumnMeta> {
        val order = if (columnOrder.isEmpty()) meta.map { it.name } else columnOrder
        return order.mapNotNull { name -> meta.find { it.name == name } }.filter { it.name !in hiddenColumns }
    }

    fun browseOptions(): TableBrowseOptions {
        val filter = when {
            columnFilter != null && columnFilterText.isNotBlank() -> columnFilterText
            globalFilter.isNotBlank() -> globalFilter
            else -> null
        }
        val col = if (columnFilter != null && columnFilterText.isNotBlank()) columnFilter else null
        return TableBrowseOptions(sortColumn, sortDesc, filter, col)
    }

    fun load(nextOffset: Long) {
        loadJob?.cancel()
        loadJob =
            scope.launch {
                loading = true
                error = null
                val started = System.nanoTime()
                runCatching {
                    withContext(Dispatchers.IO) {
                        facade.fetchTablePage(connectionId, table, nextOffset, pageSize, scopeKey, browseOptions()).get()
                    }
                }.onSuccess {
                    page = it
                    offset = it.offset
                    if (columnOrder.isEmpty()) columnOrder = it.columns.map { c -> c.name }
                    lastLoadMs = (System.nanoTime() - started) / 1_000_000
                }.onFailure {
                    if (it.message?.contains("Superseded") != true) {
                        error = it.message
                    }
                }
                loading = false
            }
    }

    LaunchedEffect(connectionId, table, sortColumn, sortDesc, globalFilter, columnFilter, columnFilterText, pageSize) {
        offset = 0
        load(0)
    }

    LaunchedEffect(hScrollBody.value) {
        if (hScrollHeader.value != hScrollBody.value) hScrollHeader.scrollTo(hScrollBody.value)
    }

    Column(modifier.fillMaxSize().padding(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(table.qualifiedName(), style = MaterialTheme.typography.titleMedium)
                page?.orderingDescription?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                lastLoadMs?.let { Text("${it}ms", style = MaterialTheme.typography.labelSmall) }
                if (loading) CircularProgressIndicator(Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
            OutlinedTextField(
                globalFilter,
                { globalFilter = it; columnFilter = null },
                label = { Text("Search all columns") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                columnFilterText,
                { columnFilterText = it },
                enabled = columnFilter != null,
                label = { Text("Column filter${columnFilter?.let { " ($it)" } ?: ""}") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 4.dp)) {
            listOf(50, 100, 200).forEach { size ->
                Button(onClick = { pageSize = size }, enabled = pageSize != size) { Text("$size/page") }
            }
            Button(onClick = { loadJob?.cancel(); loading = false; error = "Cancelled" }) { Text("Cancel") }
            Button(onClick = { load(offset) }, enabled = !loading) { Text("Refresh") }
            Button(onClick = { if (offset >= pageSize) load(offset - pageSize) }, enabled = offset > 0 && !loading) { Text("Previous") }
            Button(
                onClick = { load(offset + pageSize) },
                enabled = (page?.rows?.size ?: 0) >= pageSize && !loading,
            ) { Text("Next") }
            Button(onClick = {
                val dialog = FileDialog(null as Frame?, "Export CSV", FileDialog.SAVE)
                dialog.file = "${table.name}.csv"
                dialog.isVisible = true
                val dir = dialog.directory ?: return@Button
                val file = dialog.file ?: return@Button
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { facade.exportTableToCsv(connectionId, table, Path.of(dir, file), null) }
                    }.onSuccess { exportMessage = "Exported ${it.rowsWritten()} rows" }
                        .onFailure { error = it.message }
                }
            }) { Text("Export CSV") }
            Button(onClick = {
                val p = page ?: return@Button
                val cols = visibleColumns(p.columns)
                val json =
                    buildString {
                        append("[\n")
                        p.rows.forEachIndexed { i, row ->
                            append("  {")
                            cols.forEachIndexed { j, col ->
                                val v = row[col.name]?.toString()?.replace("\"", "\\\"") ?: ""
                                append("\"${col.name}\":\"$v\"")
                                if (j < cols.lastIndex) append(",")
                            }
                            append("}")
                            if (i < p.rows.lastIndex) append(",")
                            append("\n")
                        }
                        append("]")
                    }
                copyToClipboard(json)
                exportMessage = "Copied ${p.rows.size} rows as JSON"
            }) { Text("Copy JSON page") }
        }
        exportMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
        Text(
            "Rows ${offset + 1}–${offset + (page?.rows?.size ?: 0)} · est ${page?.totalRowsEstimate ?: "?"} total",
            style = MaterialTheme.typography.labelSmall,
        )
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        val columns = page?.let { visibleColumns(it.columns) }.orEmpty()
        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
            Row(Modifier.horizontalScroll(hScrollHeader)) {
                Text("#", Modifier.width(48.dp).padding(8.dp), style = MaterialTheme.typography.labelMedium)
                columns.forEach { col ->
                    val selected = sortColumn == col.name
                    Box {
                        Text(
                            "${col.name}\n${col.dataType}${if (col.nullable) "?" else ""}" +
                                (if (selected) if (sortDesc) " ↓" else " ↑" else ""),
                            Modifier
                                .width((columnWidths[col.name] ?: 120).dp)
                                .combinedClickable(
                                    onClick = {
                                        if (sortColumn == col.name) sortDesc = !sortDesc else {
                                            sortColumn = col.name
                                            sortDesc = false
                                        }
                                    },
                                    onLongClick = { columnMenuFor = col.name },
                                )
                                .padding(8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        DropdownMenu(expanded = columnMenuFor == col.name, onDismissRequest = { columnMenuFor = null }) {
                            DropdownMenuItem(
                                text = { Text("Filter this column") },
                                onClick = {
                                    columnFilter = col.name
                                    columnMenuFor = null
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(if (col.name in hiddenColumns) "Show" else "Hide") },
                                onClick = {
                                    hiddenColumns =
                                        if (col.name in hiddenColumns) hiddenColumns - col.name else hiddenColumns + col.name
                                    columnMenuFor = null
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Copy name") },
                                onClick = {
                                    copyToClipboard(col.name)
                                    columnMenuFor = null
                                },
                            )
                        }
                    }
                }
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(page?.rows.orEmpty()) { rowIndex, row ->
                Row(Modifier.horizontalScroll(hScrollBody).fillMaxWidth()) {
                    Text(
                        "${offset + rowIndex + 1}",
                        Modifier.width(48.dp).padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    columns.forEach { col ->
                        val raw = row[col.name]
                        val isNull = raw == null
                        val cell = if (isNull) "NULL" else raw.toString()
                        Text(
                            cell,
                            Modifier
                                .width((columnWidths[col.name] ?: 120).dp)
                                .combinedClickable(
                                    onClick = {
                                        if (cell.length > 120) longValue = cell else copyToClipboard(if (isNull) "" else cell)
                                    },
                                    onLongClick = {
                                        copyToClipboard(columns.joinToString("\t") { c ->
                                            val v = row[c.name]
                                            if (v == null) "" else v.toString()
                                        })
                                    },
                                )
                                .padding(8.dp),
                            color = if (isNull) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                            fontStyle = if (isNull) FontStyle.Italic else FontStyle.Normal,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }

    longValue?.let { value ->
        AlertDialog(
            onDismissRequest = { longValue = null },
            title = { Text("Cell value") },
            text = { Text(value) },
            confirmButton = {
                TextButton(onClick = {
                    copyToClipboard(value)
                    longValue = null
                }) { Text("Copy") }
            },
            dismissButton = { TextButton(onClick = { longValue = null }) { Text("Close") } },
        )
    }
}

private fun copyToClipboard(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
