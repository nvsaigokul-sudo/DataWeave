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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.app.preferences.UserPreferences
import java.util.UUID.fromString
import com.dataweave.app.ui.theme.DwSpacing
import com.dataweave.app.ui.browse.TableDataGrid
import com.dataweave.app.ui.connections.ConnectionsScreen
import com.dataweave.app.ui.datasets.DatasetBuilderScreen
import com.dataweave.app.ui.projects.ProjectWorkspace
import com.dataweave.app.ui.sql.SqlEditorScreen
import com.dataweave.app.ui.theme.DataWeaveTheme
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.TableRef
import java.util.UUID

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
    var darkTheme by remember { mutableStateOf(UserPreferences.loadDarkTheme()) }
    var section by remember { mutableStateOf(WorkspaceSection.Welcome) }
    var activeConnectionId by remember {
        mutableStateOf(
            UserPreferences.loadLastConnectionId()?.let { runCatching { fromString(it) }.getOrNull() },
        )
    }
    var selectedTable by remember { mutableStateOf<TableRef?>(null) }
    var pendingEditConnectionId by remember { mutableStateOf<UUID?>(null) }
    var statusBar by remember { mutableStateOf("Ready") }

    fun setActiveConnection(id: UUID?) {
        activeConnectionId = id
        UserPreferences.saveLastConnectionId(id?.toString())
    }

    DataWeaveTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                TopBar(
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        darkTheme = !darkTheme
                        UserPreferences.saveDarkTheme(darkTheme)
                    },
                    section = section,
                    onNavigate = { section = it },
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
                            connectionId = activeConnectionId,
                            onSelectTable = { table ->
                                selectedTable = table
                                section = WorkspaceSection.Browse
                                statusBar = "Selected ${table.qualifiedName()}"
                            },
                            onOpenConnections = { section = WorkspaceSection.Connections },
                            onConnectionEstablished = { id ->
                                setActiveConnection(id)
                                statusBar = "Connected"
                            },
                            modifier = Modifier.padding(DwSpacing.xs).fillMaxHeight(),
                        )
                    }
                    Box(Modifier.weight(1f).padding(DwSpacing.lg)) {
                        when (section) {
                            WorkspaceSection.Welcome ->
                                HomeConnectionHub(
                                    facade = facade,
                                    activeConnectionId = activeConnectionId,
                                    onActiveConnectionChange = { setActiveConnection(it) },
                                    onConnected = {
                                        setActiveConnection(it)
                                        section = WorkspaceSection.Browse
                                        statusBar = "Connected"
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
                                        setActiveConnection(it)
                                        section = WorkspaceSection.Browse
                                        statusBar = "Connected"
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Browse ->
                                if (activeConnectionId != null && selectedTable != null) {
                                    TableDataGrid(facade, activeConnectionId!!, selectedTable!!, Modifier.fillMaxSize())
                                } else {
                                    Text(
                                        "Connect and pick a table from the explorer sidebar, or open Connections.",
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }
                            WorkspaceSection.Sql ->
                                SqlEditorScreen(
                                    facade,
                                    activeConnectionId,
                                    onSelectConnection = { setActiveConnection(it) },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Datasets ->
                                DatasetBuilderScreen(
                                    facade,
                                    activeConnectionId,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            WorkspaceSection.Projects ->
                                ProjectWorkspace(facade, Modifier.fillMaxSize())
                        }
                    }
                }
                HorizontalDivider()
                StatusBar(activeConnectionId, facade, statusBar) { statusBar = it }
            }
        }
    }
}

@Composable
private fun TopBar(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    section: WorkspaceSection,
    onNavigate: (WorkspaceSection) -> Unit,
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
        Text("Dark theme", modifier = Modifier.padding(end = 8.dp))
        Switch(checked = darkTheme, onCheckedChange = { onToggleTheme() })
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
    onUpdate: (String) -> Unit,
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
