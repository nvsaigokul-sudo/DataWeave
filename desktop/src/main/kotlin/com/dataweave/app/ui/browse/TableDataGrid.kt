package com.dataweave.app.ui.browse

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
import com.dataweave.backend.domain.TableRef
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Path
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TableDataGrid(facade: ApplicationFacade, connectionId: UUID, table: TableRef, modifier: Modifier = Modifier) {
    var page by remember { mutableStateOf<DataPage?>(null) }
    var offset by remember { mutableStateOf(0L) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scopeKey = remember(table) { "grid-${table.schema}.${table.name}" }

    fun load(nextOffset: Long) {
        scope.launch {
            loading = true
            error = null
            runCatching {
                withContext(Dispatchers.IO) {
                    facade.fetchTablePage(connectionId, table, nextOffset, 100, scopeKey).get()
                }
            }.onSuccess {
                page = it
                offset = it.offset
            }.onFailure { error = it.message }
            loading = false
        }
    }

    LaunchedEffect(connectionId, table) {
        offset = 0
        load(0)
    }

    Card(modifier.fillMaxSize()) {
        Column(Modifier.padding(12.dp).fillMaxSize()) {
            Text("${table.qualifiedName()} — paged grid (max 100 rows per fetch)")
            page?.orderingDescription?.let { Text(it, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                Button(onClick = { if (offset >= 100) load(offset - 100) }, enabled = offset > 0 && !loading) { Text("Previous") }
                Button(onClick = { load(offset + 100) }, enabled = (page?.rows?.size ?: 0) >= 100 && !loading) { Text("Next") }
                Button(
                    onClick = {
                        val dialog = FileDialog(null as Frame?, "Export CSV", FileDialog.SAVE)
                        dialog.file = "${table.name}.csv"
                        dialog.isVisible = true
                        val dir = dialog.directory
                        val file = dialog.file
                        if (dir == null || file == null) return@Button
                        val dest = Path.of(dir, file)
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    facade.exportTableToCsv(connectionId, table, dest)
                                }
                            }.onSuccess { count ->
                                error = null
                                exportMessage = "Exported $count rows to $dest"
                            }.onFailure {
                                error = it.message
                            }
                        }
                    },
                ) {
                    Text("Export CSV")
                }
                Text("Offset $offset")
                if (loading) CircularProgressIndicator()
            }
            exportMessage?.let { Text(it) }
            error?.let { Text("Error: $it", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            val columns = page?.columns.orEmpty()
            val hScroll = rememberScrollState()
            Row(Modifier.horizontalScroll(hScroll).fillMaxWidth()) {
                columns.forEach { col ->
                    Text(col.name, Modifier.padding(8.dp), style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                items(page?.rows.orEmpty()) { row ->
                    Row(Modifier.horizontalScroll(hScroll).fillMaxWidth()) {
                        columns.forEach { col ->
                            Text(row[col.name]?.toString() ?: "NULL", Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}
