package com.dataweave.app.ui.projects

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
import com.dataweave.backend.domain.WorkspaceProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProjectWorkspace(facade: ApplicationFacade, modifier: Modifier = Modifier) {
    var projects by remember { mutableStateOf<List<WorkspaceProject>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { facade.listProjects() } }
                .onSuccess { projects = it }
                .onFailure { error = it.message }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Workspace projects")
        Text("Local SQLite metadata stores project definitions. PostgreSQL connections are configured separately.")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row {
                    Button(
                        onClick = {
                            scope.launch {
                                runCatching { withContext(Dispatchers.IO) { facade.createProject(name, description) } }
                                    .onSuccess {
                                        name = ""
                                        description = ""
                                        error = null
                                        refresh()
                                    }
                                    .onFailure { error = it.message }
                            }
                        },
                        enabled = name.isNotBlank(),
                    ) { Text("Create project") }
                }
                error?.let { Text("Error: $it", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(projects, key = { it.id }) { project ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(project.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                        if (project.description.isNotBlank()) Text(project.description)
                    }
                }
            }
        }
    }
}
