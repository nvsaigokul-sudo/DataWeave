package com.dataweave.app.ui.browse

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
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionProfile
import com.dataweave.backend.domain.TableRef
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBrowseScreen(
    facade: ApplicationFacade,
    selectedConnectionId: UUID?,
    onSelectConnection: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    var connections by remember { mutableStateOf<List<ConnectionProfile>>(emptyList()) }
    var connectionId by remember { mutableStateOf(selectedConnectionId) }
    var schemas by remember { mutableStateOf<List<String>>(emptyList()) }
    var schema by remember { mutableStateOf("public") }
    var tables by remember { mutableStateOf<List<TableRef>>(emptyList()) }
    var selectedTable by remember { mutableStateOf<TableRef?>(null) }
    var status by remember { mutableStateOf("Select a connection to browse.") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        connections = withContext(Dispatchers.IO) { facade.listConnections() }
        if (connectionId == null && connections.isNotEmpty()) {
            connectionId = connections.first().id
        }
    }

    LaunchedEffect(connectionId) {
        val id = connectionId ?: return@LaunchedEffect
        onSelectConnection(id)
        runCatching {
            withContext(Dispatchers.IO) {
                facade.refreshSchema(id)
                facade.listSchemas(id)
            }
        }.onSuccess {
            schemas = it
            if (schema !in schemas && schemas.isNotEmpty()) schema = schemas.first()
            status = "Loaded ${it.size} schemas"
        }.onFailure { status = "Schema error: ${it.message}" }
    }

    LaunchedEffect(connectionId, schema) {
        val id = connectionId ?: return@LaunchedEffect
        runCatching { withContext(Dispatchers.IO) { facade.listTables(id, schema) } }
            .onSuccess {
                tables = it
                status = "${it.size} tables in $schema"
            }
            .onFailure { status = it.message ?: "Failed to list tables" }
    }

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Database browser")
        Text(status, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConnectionPicker(connections, connectionId) {
                connectionId = it
            }
            SchemaPicker(schemas, schema) { schema = it }
            Button(
                onClick = {
                    val id = connectionId ?: return@Button
                    scope.launch {
                        withContext(Dispatchers.IO) { facade.refreshSchema(id) }
                        status = "Schema cache refreshed"
                    }
                },
            ) { Text("Refresh metadata") }
        }
        Row(Modifier.weight(0.4f)) {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(tables, key = { "${it.schema}.${it.name}" }) { table ->
                    Card(Modifier.fillMaxWidth().clickable { selectedTable = table }) {
                        Text("${table.schema}.${table.name}", Modifier.padding(12.dp))
                    }
                }
            }
        }
        selectedTable?.let { table ->
            val id = connectionId
            if (id != null) {
                TableDataGrid(facade, id, table, Modifier.weight(0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionPicker(
    connections: List<ConnectionProfile>,
    selected: UUID?,
    onSelect: (UUID) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = connections.firstOrNull { it.id == selected }?.name ?: "Choose connection"
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        OutlinedTextField(label, {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor())
        ExposedDropdownMenu(expanded, { expanded = false }) {
            connections.forEach { conn ->
                DropdownMenuItem(text = { Text(conn.name) }, onClick = { onSelect(conn.id); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchemaPicker(schemas: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        OutlinedTextField(selected, {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor())
        ExposedDropdownMenu(expanded, { expanded = false }) {
            schemas.forEach { s ->
                DropdownMenuItem(text = { Text(s) }, onClick = { onSelect(s); expanded = false })
            }
        }
    }
}
