package com.dataweave.app.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.theme.DwSpacing
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionLifecycleState
import com.dataweave.backend.domain.ConnectionProfile
import com.dataweave.backend.domain.ConnectionSessionStatus
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeConnectionHub(
    facade: ApplicationFacade,
    activeConnectionId: UUID?,
    onActiveConnectionChange: (UUID?) -> Unit,
    onConnected: (UUID) -> Unit,
    onCreateConnection: () -> Unit,
    onManageConnections: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var profiles by remember { mutableStateOf<List<ConnectionProfile>>(emptyList()) }
    var statuses by remember { mutableStateOf<Map<UUID, ConnectionSessionStatus>>(emptyMap()) }
    var busyId by remember { mutableStateOf<UUID?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            profiles = withContext(Dispatchers.IO) { facade.listConnections() }
            statuses =
                withContext(Dispatchers.IO) {
                    facade.allConnectionStatuses().associateBy { it.connectionId() }
                }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    fun connect(id: UUID) {
        busyId = id
        message = null
        scope.launch {
            try {
                val st = withContext(Dispatchers.IO) { facade.connectConnection(id) }
                statuses = statuses + (id to st)
                onActiveConnectionChange(id)
                message = st.message()
                if (st.state() == ConnectionLifecycleState.CONNECTED) {
                    onConnected(id)
                }
            } catch (ex: Exception) {
                message = ex.message ?: "Connect failed"
            } finally {
                busyId = null
                refresh()
            }
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(DwSpacing.md)) {
        Text("Welcome to DataWeave", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Connect to PostgreSQL to browse schemas, run SQL, and build datasets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val activeStatus = activeConnectionId?.let { statuses[it] }
        if (activeConnectionId != null && activeStatus != null) {
            ConnectionStatusCard(
                profile = profiles.firstOrNull { it.id == activeConnectionId },
                status = activeStatus,
                onConnect = { connect(activeConnectionId) },
                onRetry = { connect(activeConnectionId) },
                onDisconnect = {
                    scope.launch {
                        withContext(Dispatchers.IO) { facade.disconnectConnection(activeConnectionId) }
                        refresh()
                    }
                },
                busy = busyId == activeConnectionId,
            )
        }

        Text("Saved connections", style = MaterialTheme.typography.titleMedium)
        if (profiles.isEmpty()) {
            Text(
                "No saved profiles yet. Create one to store host, database, and credentials in the local vault.",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCreateConnection) { Text("Create connection") }
                OutlinedButton(onClick = onManageConnections) { Text("Open Connections") }
            }
        } else {
            profiles.forEach { profile ->
                val st = statuses[profile.id]
                SavedProfileRow(
                    profile = profile,
                    status = st,
                    selected = profile.id == activeConnectionId,
                    busy = busyId == profile.id,
                    onSelect = { onActiveConnectionChange(profile.id) },
                    onConnect = { connect(profile.id) },
                    onEdit = onManageConnections,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCreateConnection) { Text("New connection") }
                TextButton(onClick = onManageConnections) { Text("Manage all") }
            }
        }

        message?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    profile: ConnectionProfile?,
    status: ConnectionSessionStatus,
    onConnect: () -> Unit,
    onRetry: () -> Unit,
    onDisconnect: () -> Unit,
    busy: Boolean,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(DwSpacing.md), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Active profile", style = MaterialTheme.typography.labelLarge)
            Text(profile?.name ?: status.connectionId().toString(), style = MaterialTheme.typography.titleMedium)
            Text(
                "${status.state()}: ${status.message()}",
                style = MaterialTheme.typography.bodySmall,
                color =
                    when (status.state()) {
                        ConnectionLifecycleState.CONNECTED -> MaterialTheme.colorScheme.primary
                        ConnectionLifecycleState.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (status.state()) {
                    ConnectionLifecycleState.CONNECTED ->
                        OutlinedButton(onClick = onDisconnect, enabled = !busy) { Text("Disconnect") }
                    ConnectionLifecycleState.ERROR ->
                        Button(onClick = onRetry, enabled = !busy) { Text(if (busy) "Retrying…" else "Retry") }
                    else ->
                        Button(onClick = onConnect, enabled = !busy) { Text(if (busy) "Connecting…" else "Connect") }
                }
            }
        }
    }
}

@Composable
private fun SavedProfileRow(
    profile: ConnectionProfile,
    status: ConnectionSessionStatus?,
    selected: Boolean,
    busy: Boolean,
    onSelect: () -> Unit,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
) {
    val state = status?.state() ?: ConnectionLifecycleState.DISCONNECTED
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(DwSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${profile.host}:${profile.port} / ${profile.databaseName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stateLabel(state, status?.message()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (state != ConnectionLifecycleState.CONNECTED) {
                    Button(onClick = onConnect, enabled = !busy) {
                        Text(if (busy) "…" else "Connect")
                    }
                }
                TextButton(onClick = onEdit) { Text("Edit") }
            }
        }
    }
}

private fun stateLabel(state: ConnectionLifecycleState, message: String?): String =
    when (state) {
        ConnectionLifecycleState.CONNECTED -> "Connected"
        ConnectionLifecycleState.CONNECTING, ConnectionLifecycleState.RECONNECTING -> "Connecting…"
        ConnectionLifecycleState.ERROR -> "Error${message?.let { ": $it" } ?: ""}"
        ConnectionLifecycleState.DISCONNECTED -> "Saved (not connected)"
    }
