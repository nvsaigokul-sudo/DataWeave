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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.projects.ProjectWorkspace
import com.dataweave.app.ui.theme.DataWeaveTheme
import com.dataweave.service.ProjectService

@Composable
fun AppShell(projectService: ProjectService) {
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
                        "Offline-first workbench",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                    )
                    NavItem("Projects", selected = true)
                    NavItem("Sources", enabled = false)
                    NavItem("Pipelines", enabled = false)
                    NavItem("Settings", enabled = false)
                }
                ProjectWorkspace(
                    projectService = projectService,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean = false, enabled: Boolean = true) {
    val color = when {
        selected -> MaterialTheme.colorScheme.primary
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = label,
        color = color,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}
