package com.dataweave.app.ui.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.app.preferences.ThemeMode
import com.dataweave.app.preferences.UserPreferences
import com.dataweave.app.ui.browse.DataWorkspaceScreen
import com.dataweave.app.ui.connections.ConnectionsScreen
import com.dataweave.app.ui.datasets.DatasetBuilderScreen
import com.dataweave.app.ui.projects.ProjectWorkspace
import com.dataweave.app.ui.settings.SettingsDialog
import com.dataweave.app.ui.sql.SqlEditorScreen
import com.dataweave.app.ui.theme.DataWeaveTheme
import com.dataweave.app.ui.theme.DwSpacing
import com.dataweave.backend.ApplicationFacade
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class WorkspaceSection {
    Welcome,
    Connections,
    Browse,
    Sql,
    Datasets,
    Projects,
}

@Composable
fun WorkspaceShell(facade: ApplicationFacade) {
    val workspace = remember { WorkspaceContext() }

    LaunchedEffect(workspace.activeConnectionId) {
        val id = workspace.activeConnectionId ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                val profile = facade.listConnections().firstOrNull { it.id == id }
                workspace.updateActiveDatabase(profile?.databaseName)
            }
        }
    }
    var themeMode by remember { mutableStateOf(UserPreferences.loadThemeMode()) }
    var section by remember { mutableStateOf(WorkspaceSection.Welcome) }
    var pendingEditConnectionId by remember { mutableStateOf<UUID?>(null) }
    var settingsOpen by remember { mutableStateOf(false) }

    DataWeaveTheme(themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                TopBar(
                    section = section,
                    onNavigate = { section = it },
                    onOpenSettings = { settingsOpen = true },
                )
                HorizontalDivider()
                Row(Modifier.weight(1f)) {
                    Surface(
                        Modifier
                            .fillMaxHeight()
                            .padding(DwSpacing.sm),
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        DatabaseTreeSidebar(
                            facade = facade,
                            connectionId = workspace.activeConnectionId,
                            onSelectTable = { table ->
                                workspace.selectTable(table)
                                section = WorkspaceSection.Browse
                            },
                            onOpenConnections = { section = WorkspaceSection.Connections },
                            onEditConnection = { id ->
                                pendingEditConnectionId = id
                                section = WorkspaceSection.Connections
                            },
                            onConnectionEstablished = { id ->
                                workspace.setActiveConnection(id)
                                workspace.updateStatus("Connected")
                            },
                            onDatabaseChanged = { workspace.updateActiveDatabase(it) },
                            modifier = Modifier.padding(DwSpacing.xs).fillMaxHeight(),
                        )
                    }
                    Box(Modifier.weight(1f).padding(DwSpacing.lg)) {
                        when (section) {
                            WorkspaceSection.Welcome ->
                                HomeConnectionHub(
                                    facade = facade,
                                    activeConnectionId = workspace.activeConnectionId,
                                    onActiveConnectionChange = { workspace.setActiveConnection(it) },
                                    onConnected = {
                                        workspace.setActiveConnection(it)
                                        section = WorkspaceSection.Browse
                                        workspace.updateStatus("Connected")
                                    },
                                    onCreateConnection = {
                                        pendingEditConnectionId = null
                                        section = WorkspaceSection.Connections
                                    },
                                    onManageConnections = { section = WorkspaceSection.Connections },
                                    onEditProfile = { id ->
                                        pendingEditConnectionId = id
                                        section = WorkspaceSection.Connections
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Connections ->
                                ConnectionsScreen(
                                    facade = facade,
                                    initialEditConnectionId = pendingEditConnectionId,
                                    onConsumedEditConnectionId = { pendingEditConnectionId = null },
                                    onConnected = {
                                        workspace.setActiveConnection(it)
                                        section = WorkspaceSection.Browse
                                        workspace.updateStatus("Connected")
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Browse ->
                                DataWorkspaceScreen(
                                    facade = facade,
                                    connectionId = workspace.activeConnectionId,
                                    selectedTable = workspace.selectedTable,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Sql ->
                                SqlEditorScreen(
                                    facade,
                                    workspace.activeConnectionId,
                                    onSelectConnection = { workspace.setActiveConnection(it) },
                                    workspace = workspace,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Datasets ->
                                DatasetBuilderScreen(
                                    facade,
                                    workspace.activeConnectionId,
                                    workspace.selectedSchema,
                                    workspace = workspace,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Projects ->
                                ProjectWorkspace(
                                    facade = facade,
                                    workspace = workspace,
                                    onNavigateToDatasets = { section = WorkspaceSection.Datasets },
                                    onNavigateToSql = { section = WorkspaceSection.Sql },
                                    modifier = Modifier.fillMaxSize(),
                                )
                        }
                    }
                }
                HorizontalDivider()
                StatusBar(workspace.activeConnectionId, facade, workspace.statusMessage)
            }
        }
    }

    if (settingsOpen) {
        SettingsDialog(
            themeMode = themeMode,
            onThemeModeChange = {
                themeMode = it
                UserPreferences.saveThemeMode(it)
            },
            onDismiss = { settingsOpen = false },
        )
    }
}

@Composable
private fun TopBar(
    section: WorkspaceSection,
    onNavigate: (WorkspaceSection) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("DataWeave", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 24.dp))
        NavTab("Home", section == WorkspaceSection.Welcome) { onNavigate(WorkspaceSection.Welcome) }
        NavTab("Connections", section == WorkspaceSection.Connections) { onNavigate(WorkspaceSection.Connections) }
        NavTab("Data", section == WorkspaceSection.Browse) { onNavigate(WorkspaceSection.Browse) }
        NavTab("SQL", section == WorkspaceSection.Sql) { onNavigate(WorkspaceSection.Sql) }
        NavTab("Datasets", section == WorkspaceSection.Datasets) { onNavigate(WorkspaceSection.Datasets) }
        NavTab("Projects", section == WorkspaceSection.Projects) { onNavigate(WorkspaceSection.Projects) }
        Box(Modifier.weight(1f))
        TextButton(onClick = onOpenSettings) { Text("Settings") }
    }
}

@Composable
private fun NavTab(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StatusBar(
    connectionId: UUID?,
    facade: ApplicationFacade,
    message: String,
) {
    val connLabel =
        connectionId?.let { id ->
            val st = facade.connectionStatus(id)
            "${st.state()} — ${st.message()}"
        } ?: "Disconnected"
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(connLabel, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
        Text(message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
