package com.dataweave.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.browse.DataBrowseScreen
import com.dataweave.app.ui.connections.ConnectionsScreen
import com.dataweave.app.ui.projects.ProjectWorkspace
import com.dataweave.app.ui.theme.DataWeaveTheme
import com.dataweave.backend.ApplicationFacade

enum class MainSection {
    Projects,
    Connections,
    Browse,
}

@Composable
fun AppShell(facade: ApplicationFacade) {
    var section by remember { mutableStateOf(MainSection.Connections) }
    var activeConnectionId by remember { mutableStateOf<java.util.UUID?>(null) }

    DataWeaveTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .padding(16.dp),
                ) {
                    Text("DataWeave", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Local-first PostgreSQL workbench",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                    )
                    NavButton("Projects", section == MainSection.Projects) { section = MainSection.Projects }
                    NavButton("Connections", section == MainSection.Connections) { section = MainSection.Connections }
                    NavButton("Browse data", section == MainSection.Browse) { section = MainSection.Browse }
                }
                when (section) {
                    MainSection.Projects -> ProjectWorkspace(facade, Modifier.weight(1f).padding(16.dp))
                    MainSection.Connections ->
                        ConnectionsScreen(
                            facade,
                            onConnected = {
                                activeConnectionId = it
                                section = MainSection.Browse
                            },
                            modifier = Modifier.weight(1f).padding(16.dp),
                        )
                    MainSection.Browse ->
                        DataBrowseScreen(
                            facade,
                            activeConnectionId,
                            onSelectConnection = { activeConnectionId = it },
                            modifier = Modifier.weight(1f).padding(16.dp),
                        )
                }
            }
        }
    }
}

@Composable
private fun NavButton(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
