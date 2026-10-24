package com.dataweave.app.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.workspace.WorkspaceContext
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.DatasetOutputDefinition
import com.dataweave.backend.domain.ProjectItem
import com.dataweave.backend.domain.WorkspaceProject
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWorkspace(
    facade: ApplicationFacade,
    workspace: WorkspaceContext,
    onNavigateToDatasets: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var projects by remember { mutableStateOf<List<WorkspaceProject>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<WorkspaceProject?>(null) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var deleteConfirm by remember { mutableStateOf<WorkspaceProject?>(null) }
    val expandedProjects = remember { mutableStateMapOf<UUID, Boolean>() }
    val projectItems = remember { mutableStateMapOf<UUID, List<ProjectItem>>() }
    val projectDatasets = remember { mutableStateMapOf<UUID, List<DatasetOutputDefinition>>() }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { facade.listProjects() } }
                .onSuccess { projects = it }
                .onFailure { error = it.message }
        }
    }

    fun loadProjectDetails(projectId: UUID) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    facade.listProjectItems(projectId) to facade.listDatasetDefinitions(projectId)
                }
            }.onSuccess { (items, datasets) ->
                projectItems[projectId] = items
                projectDatasets[projectId] = datasets
            }.onFailure { error = it.message }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Workspace projects")
        Text("Local SQLite metadata stores project definitions. Link saved datasets to reopen them from a project.")
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
                error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(projects, key = { it.id }) { project ->
                val expanded = expandedProjects[project.id] == true
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                expandedProjects[project.id] = !expanded
                                if (!expanded) loadProjectDetails(project.id)
                            }) { Text(if (expanded) "Hide items" else "Show items") }
                            Text(project.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        }
                        if (project.description.isNotBlank()) Text(project.description)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                editing = project
                                editName = project.name
                                editDescription = project.description
                            }) { Text("Rename") }
                            Button(onClick = {
                                scope.launch {
                                    runCatching { withContext(Dispatchers.IO) { facade.duplicateProject(project.id) } }
                                        .onSuccess { refresh() }
                                        .onFailure { error = it.message }
                                }
                            }) { Text("Duplicate") }
                            Button(onClick = { deleteConfirm = project }) { Text("Delete") }
                        }
                        if (expanded) {
                            ProjectItemsPanel(
                                project = project,
                                items = projectItems[project.id].orEmpty(),
                                datasets = projectDatasets[project.id].orEmpty(),
                                onLink = { datasetId, label ->
                                    scope.launch {
                                        runCatching {
                                            withContext(Dispatchers.IO) {
                                                facade.addDatasetProjectItem(project.id, datasetId, label)
                                            }
                                        }.onSuccess { loadProjectDetails(project.id) }
                                            .onFailure { error = it.message }
                                    }
                                },
                                onRemove = { itemId ->
                                    scope.launch {
                                        runCatching { withContext(Dispatchers.IO) { facade.removeProjectItem(itemId) } }
                                            .onSuccess { loadProjectDetails(project.id) }
                                            .onFailure { error = it.message }
                                    }
                                },
                                onOpenDataset = { item ->
                                    scope.launch {
                                        runCatching { withContext(Dispatchers.IO) { facade.getDatasetDefinition(item.refId()) } }
                                            .onSuccess { def ->
                                                workspace.openDatasetInEditor(project.id, def.id, def.connectionId)
                                                onNavigateToDatasets()
                                            }
                                            .onFailure { error = "Cannot open dataset: ${it.message}" }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    editing?.let { project ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Rename project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(editName, { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(editDescription, { editDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    facade.updateProject(project.id, editName, editDescription)
                                }
                            }.onSuccess {
                                editing = null
                                refresh()
                            }.onFailure { error = it.message }
                        }
                    },
                    enabled = editName.isNotBlank(),
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
        )
    }

    deleteConfirm?.let { project ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            title = { Text("Delete project?") },
            text = { Text("Remove \"${project.name}\" and its local references. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            runCatching { withContext(Dispatchers.IO) { facade.deleteProject(project.id) } }
                                .onSuccess {
                                    deleteConfirm = null
                                    projectItems.remove(project.id)
                                    projectDatasets.remove(project.id)
                                    refresh()
                                }
                                .onFailure { error = it.message }
                        }
                    },
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteConfirm = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectItemsPanel(
    project: WorkspaceProject,
    items: List<ProjectItem>,
    datasets: List<DatasetOutputDefinition>,
    onLink: (UUID, String) -> Unit,
    onRemove: (UUID) -> Unit,
    onOpenDataset: (ProjectItem) -> Unit,
) {
    var linkExpanded by remember { mutableStateOf(false) }
    var selectedDataset by remember { mutableStateOf<DatasetOutputDefinition?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Linked items", style = MaterialTheme.typography.titleSmall)
        if (items.isEmpty()) {
            Text("No linked datasets yet.", style = MaterialTheme.typography.bodySmall)
        } else {
            items.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${item.label()} (${item.itemType()})", modifier = Modifier.weight(1f))
                    if (item.itemType() == ProjectItem.TYPE_DATASET) {
                        Button(onClick = { onOpenDataset(item) }) { Text("Open in Datasets") }
                    }
                    Button(onClick = { onRemove(item.id()) }) { Text("Remove link") }
                }
            }
        }
        if (datasets.isEmpty()) {
            Text("Save a dataset under Datasets for this project before linking.", style = MaterialTheme.typography.bodySmall)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { linkExpanded = true }) {
                    Text(selectedDataset?.let { "${it.sourceSchema}.${it.sourceTable}" } ?: "Choose dataset…")
                }
                DropdownMenu(expanded = linkExpanded, onDismissRequest = { linkExpanded = false }) {
                    datasets.forEach { def ->
                        DropdownMenuItem(
                            text = { Text("${def.sourceSchema}.${def.sourceTable}") },
                            onClick = {
                                selectedDataset = def
                                linkExpanded = false
                            },
                        )
                    }
                }
                Button(
                    onClick = {
                        val def = selectedDataset ?: return@Button
                        onLink(def.id, "${def.sourceSchema}.${def.sourceTable}")
                        selectedDataset = null
                    },
                    enabled = selectedDataset != null,
                ) { Text("Link dataset") }
            }
        }
    }
}
