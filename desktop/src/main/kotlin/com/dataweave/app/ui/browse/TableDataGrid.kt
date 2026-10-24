package com.dataweave.app.ui.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.backend.ApplicationFacade
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableDataGrid(facade: ApplicationFacade, connectionId: UUID, table: TableRef, modifier: Modifier = Modifier) {
    var page by remember { mutableStateOf<DataPage?>(null) }
    var offset by remember { mutableStateOf(0L) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var filterText by remember { mutableStateOf("") }
    var sortColumn by remember { mutableStateOf<String?>(null) }
    var sortDesc by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scopeKey = remember(table) { "grid-${table.schema}.${table.name}" }

    fun browseOptions(): TableBrowseOptions =
        TableBrowseOptions(sortColumn, sortDesc, filterText.ifBlank { null }, null)

    fun load(nextOffset: Long) {
        scope.launch {
            loading = true
            error = null
            runCatching {
                withContext(Dispatchers.IO) {
                    facade.fetchTablePage(connectionId, table, nextOffset, 100, scopeKey, browseOptions()).get()
                }
            }.onSuccess {
                page = it
                offset = it.offset
            }.onFailure { error = it.message }
            loading = false
        }
    }

    LaunchedEffect(connectionId, table, sortColumn, sortDesc, filterText) {
        offset = 0
        load(0)
    }

    Card(modifier.fillMaxSize()) {
        Column(Modifier.padding(12.dp).fillMaxSize()) {
            Text("${table.qualifiedName()} — server-paged grid")
            page?.orderingDescription?.let { Text(it, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
            OutlinedTextField(
                filterText,
                { filterText = it },
                label = { Text("Filter (server ILIKE across columns)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                Button(onClick = { if (offset >= 100) load(offset - 100) }, enabled = offset > 0 && !loading) { Text("Previous") }
                Button(onClick = { load(offset + 100) }, enabled = (page?.rows?.size ?: 0) >= 100 && !loading) { Text("Next") }
                Button(
                    onClick = {
                        val dialog = FileDialog(null as Frame?, "Export CSV (full table stream)", FileDialog.SAVE)
                        dialog.file = "${table.name}.csv"
                        dialog.isVisible = true
                        val dir = dialog.directory
                        val file = dialog.file
                        if (dir == null || file == null) return@Button
                        val dest = Path.of(dir, file)
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    facade.exportTableToCsv(connectionId, table, dest, null)
                                }
                            }.onSuccess { result ->
                                error = null
                                exportMessage =
                                    "Exported ${result.rowsWritten()} rows" +
                                        if (result.truncated()) " (limit ${result.rowLimitApplied()} reached)" else ""
                            }.onFailure {
                                error = it.message
                            }
                        }
                    },
                ) {
                    Text("Export CSV")
                }
                Text("Offset $offset · est ${page?.totalRowsEstimate ?: "?"} rows")
                if (loading) CircularProgressIndicator()
            }
            exportMessage?.let { Text(it) }
            error?.let { Text("Error: $it", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            val columns = page?.columns.orEmpty()
            val hScroll = rememberScrollState()
            Row(Modifier.horizontalScroll(hScroll).fillMaxWidth()) {
                columns.forEach { col ->
                    val selected = sortColumn == col.name
                    Text(
                        "${col.name}\n${col.dataType}${if (col.nullable) "?" else ""}" +
                            (if (selected) if (sortDesc) " ↓" else " ↑" else ""),
                        Modifier
                            .combinedClickable(
                                onClick = {
                                    if (sortColumn == col.name) {
                                        sortDesc = !sortDesc
                                    } else {
                                        sortColumn = col.name
                                        sortDesc = false
                                    }
                                },
                                onLongClick = {
                                    copyToClipboard(col.name)
                                },
                            )
                            .padding(8.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    )
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                items(page?.rows.orEmpty().withIndex().toList()) { (rowIndex, row) ->
                    Row(Modifier.horizontalScroll(hScroll).fillMaxWidth()) {
                        Text(
                            "${offset + rowIndex + 1}",
                            Modifier.padding(8.dp),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        columns.forEach { col ->
                            val raw = row[col.name]
                            val isNull = raw == null
                            val cell = if (isNull) "NULL" else raw.toString()
                            Text(
                                cell,
                                Modifier
                                    .combinedClickable(
                                        onClick = { copyToClipboard(if (isNull) "" else cell) },
                                        onLongClick = {
                                            copyToClipboard(
                                                columns.joinToString("\t") {
                                                    val v = row[it.name]
                                                    if (v == null) "" else v.toString()
                                                },
                                            )
                                        },
                                    )
                                    .padding(8.dp),
                                color =
                                    if (isNull) {
                                        MaterialTheme.colorScheme.outline
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                fontStyle =
                                    if (isNull) {
                                        androidx.compose.ui.text.font.FontStyle.Italic
                                    } else {
                                        androidx.compose.ui.text.font.FontStyle.Normal
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(text: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
