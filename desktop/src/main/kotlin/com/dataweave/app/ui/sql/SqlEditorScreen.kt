package com.dataweave.app.ui.sql

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.common.ResultDataCard
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionProfile
import com.dataweave.backend.domain.DataPage
import com.dataweave.backend.domain.SqlQueryHistoryEntry
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SqlEditorScreen(
    facade: ApplicationFacade,
    activeConnectionId: UUID?,
    onSelectConnection: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    var connections by remember { mutableStateOf<List<ConnectionProfile>>(emptyList()) }
    var connectionId by remember { mutableStateOf(activeConnectionId) }
    var sql by remember { mutableStateOf("SELECT 1 AS ok") }
    var page by remember { mutableStateOf<DataPage?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf<List<SqlQueryHistoryEntry>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun refreshHistory() {
        scope.launch {
            history = withContext(Dispatchers.IO) { facade.sqlQueryHistory(30) }
        }
    }

    LaunchedEffect(Unit) {
        connections = withContext(Dispatchers.IO) { facade.listConnections() }
        if (connectionId == null && connections.isNotEmpty()) {
            connectionId = connections.first().id
        }
        refreshHistory()
    }

    LaunchedEffect(activeConnectionId) {
        if (activeConnectionId != null) {
            connectionId = activeConnectionId
        }
    }

    LaunchedEffect(connectionId) {
        connectionId?.let { onSelectConnection(it) }
    }

    Column(modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("SQL editor (read-only SELECT)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            connections.forEach { conn ->
                Button(onClick = { connectionId = conn.id }) {
                    Text(if (connectionId == conn.id) "[${conn.name}]" else conn.name)
                }
            }
        }
        OutlinedTextField(
            value = sql,
            onValueChange = { sql = it },
            modifier = Modifier.fillMaxWidth().weight(0.35f, fill = false),
            minLines = 4,
            label = { Text("SQL") },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = connectionId != null && status != "Running…",
                onClick = {
                    val id = connectionId ?: return@Button
                    scope.launch {
                        status = "Running…"
                        error = null
                        runCatching {
                            withContext(Dispatchers.IO) { facade.runSqlEditorQuery(id, sql) }
                        }.onSuccess {
                            page = it
                            status = "OK (${it.rows.size} rows)"
                            refreshHistory()
                        }.onFailure {
                            page = null
                            error = it.message
                            status = "Failed"
                            refreshHistory()
                        }
                    }
                },
            ) {
                Text("Run")
            }
            OutlinedButton(
                onClick = {
                    sql = ""
                    page = null
                    error = null
                    status = ""
                },
            ) {
                Text("Clear")
            }
            Text(status)
        }
        ResultDataCard(page, error, Modifier.weight(1f))
        Text("History")
        LazyColumn(Modifier.weight(0.4f)) {
            items(history) { entry ->
                Text(
                    "${entry.executedAt} · ${entry.rowCount} rows · ${entry.sqlText.take(120)}",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                sql = entry.sqlText
                                entry.connectionId.ifPresent { connectionId = it }
                            }
                            .padding(4.dp),
                )
            }
        }
    }
}
