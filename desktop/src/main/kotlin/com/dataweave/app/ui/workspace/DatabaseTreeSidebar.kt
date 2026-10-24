package com.dataweave.app.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionLifecycleState
import com.dataweave.backend.domain.TableRef
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DatabaseTreeSidebar(
    facade: ApplicationFacade,
    connectionId: UUID?,
    onSelectTable: (TableRef) -> Unit,
    modifier: Modifier = Modifier,
) {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var schemas by remember { mutableStateOf<List<String>>(emptyList()) }
    val expandedSchemas = remember { mutableStateMapOf<String, Boolean>() }
    val tablesBySchema = remember { mutableStateMapOf<String, List<TableRef>>() }
    val loadGeneration = remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    fun loadTables(schema: String) {
        val id = connectionId ?: return
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { facade.listTables(id, schema) } }
                .onSuccess { tablesBySchema[schema] = it }
        }
    }

    LaunchedEffect(connectionId) {
        val id = connectionId ?: return@LaunchedEffect
        val gen = loadGeneration.value + 1
        loadGeneration.value = gen
        loading = true
        error = null
        schemas = emptyList()
        tablesBySchema.clear()
        val status = withContext(Dispatchers.IO) { facade.connectionStatus(id) }
        if (status.state() != ConnectionLifecycleState.CONNECTED) {
            loading = false
            error = "Connect to load metadata"
            return@LaunchedEffect
        }
        runCatching {
            withContext(Dispatchers.IO) { facade.listSchemas(id) }
        }.onSuccess {
            if (loadGeneration.value == gen) {
                schemas = it
                if (it.isNotEmpty()) {
                    expandedSchemas[it.first()] = true
                    loadTables(it.first())
                }
            }
        }.onFailure {
            if (loadGeneration.value == gen) error = it.message
        }
        loading = false
    }

    Column(modifier) {
        Text("Database explorer", modifier = Modifier.padding(8.dp))
        when {
            loading -> CircularProgressIndicator(Modifier.padding(8.dp))
            error != null -> Text(error!!, modifier = Modifier.padding(8.dp))
            connectionId == null -> Text("No connection selected", modifier = Modifier.padding(8.dp))
            else ->
                LazyColumn {
                    items(schemas) { schema ->
                        val expanded = expandedSchemas[schema] == true
                        Text(
                            (if (expanded) "▼ " else "▶ ") + schema,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val nowExpanded = expandedSchemas[schema] != true
                                        expandedSchemas[schema] = nowExpanded
                                        if (nowExpanded && !tablesBySchema.containsKey(schema)) loadTables(schema)
                                    }
                                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                        )
                        if (expanded) {
                            val tables = tablesBySchema[schema].orEmpty()
                            if (tables.isEmpty()) {
                                Text("Loading…", modifier = Modifier.padding(start = 24.dp, bottom = 4.dp))
                            } else {
                                tables.forEach { table ->
                                    Text(
                                        table.name,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelectTable(table) }
                                                .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}
