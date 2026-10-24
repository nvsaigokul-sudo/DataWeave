package com.dataweave.app.ui.connections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dataweave.app.platform.WindowsPostgresServiceHelper
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionLifecycleState
import com.dataweave.backend.domain.ConnectionProfile
import com.dataweave.backend.domain.ConnectionSessionStatus
import com.dataweave.backend.domain.SslMode
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ConnectionsScreen(
    facade: ApplicationFacade,
    onConnected: (UUID) -> Unit,
    initialEditConnectionId: UUID? = null,
    onConsumedEditConnectionId: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var connections by remember { mutableStateOf<List<ConnectionProfile>>(emptyList()) }
    var statuses by remember { mutableStateOf<Map<UUID, ConnectionSessionStatus>>(emptyMap()) }
    var editingId by remember { mutableStateOf<UUID?>(null) }
    var name by remember { mutableStateOf("Local PostgreSQL") }
    var host by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("5432") }
    var database by remember { mutableStateOf("postgres") }
    var username by remember { mutableStateOf("postgres") }
    var password by remember { mutableStateOf("") }
    var sslMode by remember { mutableStateOf(SslMode.PREFER) }
    var timeout by remember { mutableStateOf("10") }
    var message by remember { mutableStateOf<String?>(null) }
    var advancedOpen by remember { mutableStateOf(false) }
    var busyAction by remember { mutableStateOf<String?>(null) }
    var pendingDeleteId by remember { mutableStateOf<UUID?>(null) }
    val scope = rememberCoroutineScope()

    fun resetNewForm() {
        editingId = null
        password = ""
        name = "Local PostgreSQL"
        host = "127.0.0.1"
        port = "5432"
        database = "postgres"
        username = "postgres"
        sslMode = SslMode.PREFER
        timeout = "10"
        message = "New connection form"
    }

    fun refresh() {
        scope.launch {
            connections = withContext(Dispatchers.IO) { facade.listConnections() }
            statuses =
                withContext(Dispatchers.IO) {
                    facade.allConnectionStatuses().associateBy { it.connectionId() }
                }
        }
    }

    fun loadProfile(p: ConnectionProfile) {
        editingId = p.id
        name = p.name
        host = p.host
        port = p.port.toString()
        database = p.databaseName
        username = p.username
        password = ""
        sslMode = p.sslMode
        timeout = p.connectTimeoutSeconds.toString()
    }

    LaunchedEffect(Unit) { refresh() }

    LaunchedEffect(initialEditConnectionId, connections) {
        val id = initialEditConnectionId ?: return@LaunchedEffect
        connections.firstOrNull { it.id == id }?.let {
            loadProfile(it)
            message = "Editing ${it.name}"
            onConsumedEditConnectionId()
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("PostgreSQL connections")
        Text(
            "Save stores profile + vault password. Connect opens a live pool. Test probes without connecting.",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        )
        if (WindowsPostgresServiceHelper.isWindows()) {
            WindowsServiceHints()
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(host, { host = it }, label = { Text("Host") }, modifier = Modifier.weight(2f))
                    OutlinedTextField(port, { port = it }, label = { Text("Port") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(database, { database = it }, label = { Text("Database") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(username, { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    password,
                    { password = it },
                    label = { Text(if (editingId == null) "Password" else "Password (leave blank to keep)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = { advancedOpen = !advancedOpen }) {
                    Text(if (advancedOpen) "Hide advanced" else "Advanced connection options")
                }
                AnimatedVisibility(visible = advancedOpen) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "jdbc:postgresql://$host:${port.toIntOrNull() ?: 5432}/$database?sslmode=${sslMode.name.lowercase()}",
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SslMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = sslMode == mode,
                                    onClick = { sslMode = mode },
                                    label = { Text(mode.name.lowercase()) },
                                )
                            }
                        }
                        OutlinedTextField(
                            timeout,
                            { timeout = it },
                            label = { Text("Timeout (seconds)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = busyAction == null,
                        onClick = {
                            scope.launch {
                                busyAction = "save"
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        val pass = password.toCharArray()
                                        val t = timeout.toIntOrNull() ?: 10
                                        val p = port.toIntOrNull() ?: 5432
                                        if (editingId == null) {
                                            facade.saveConnection(name, host, p, database, username, pass, sslMode, t)
                                        } else {
                                            val pwd = if (password.isBlank()) null else pass
                                            facade.updateConnection(editingId!!, name, host, p, database, username, pwd, sslMode, t)
                                        }
                                    }
                                }.onSuccess {
                                    password = ""
                                    message = "Saved ${it.name} (not connected until you press Connect)"
                                    editingId = it.id
                                    refresh()
                                }.onFailure { message = it.message }
                                busyAction = null
                            }
                        },
                    ) { Text("Save") }
                    Button(onClick = { resetNewForm() }) { Text("New") }
                }
                message?.let { Text(it) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            connections.forEach { conn ->
                val status = statuses[conn.id]
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(conn.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                                Text("${conn.username}@${conn.host}:${conn.port}/${conn.databaseName}")
                                Text(
                                    statusLabel(status),
                                    color = statusColor(status),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                )
                            }
                            Button(onClick = { loadProfile(conn) }) { Text("Edit") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                enabled = busyAction == null,
                                onClick = {
                                    scope.launch {
                                        busyAction = "test-${conn.id}"
                                        testConn(facade, conn.id, password, { refresh() }) { message = it }
                                        busyAction = null
                                    }
                                },
                            ) {
                                Text("Test")
                            }
                            Button(
                                enabled = busyAction == null,
                                onClick = {
                                    scope.launch {
                                        busyAction = "connect-${conn.id}"
                                        connectConn(facade, conn.id, password) { msg, ok ->
                                            message = msg
                                            refresh()
                                            if (ok) onConnected(conn.id)
                                        }
                                        busyAction = null
                                    }
                                },
                            ) { Text("Connect") }
                            Button(
                                enabled = busyAction == null,
                                onClick = {
                                    scope.launch {
                                        busyAction = "disconnect-${conn.id}"
                                        disconnectConn(facade, conn.id, { refresh() }) { message = it }
                                        busyAction = null
                                    }
                                },
                            ) {
                                Text("Disconnect")
                            }
                            Button(
                                enabled = busyAction == null,
                                onClick = {
                                    scope.launch {
                                        busyAction = "reconnect-${conn.id}"
                                        reconnectConn(facade, conn.id, password, { refresh() }) { message = it }
                                        busyAction = null
                                    }
                                },
                            ) {
                                Text("Reconnect")
                            }
                            Button(
                                enabled = busyAction == null,
                                onClick = {
                                    scope.launch {
                                        busyAction = "refresh-${conn.id}"
                                        runCatching { withContext(Dispatchers.IO) { facade.refreshSchema(conn.id) } }
                                            .onSuccess { message = "Metadata cache refreshed (v$it)" }
                                            .onFailure { message = it.message }
                                        busyAction = null
                                    }
                                },
                            ) { Text("Refresh metadata") }
                            Button(onClick = { pendingDeleteId = conn.id }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteId?.let { deleteId ->
        val conn = connections.firstOrNull { it.id == deleteId }
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete connection?") },
            text = {
                Text(
                    conn?.let { "Remove \"${it.name}\" and its saved credentials from this device?" }
                        ?: "Remove this connection profile?",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            busyAction = "delete"
                            withContext(Dispatchers.IO) { facade.deleteConnection(deleteId) }
                            if (editingId == deleteId) resetNewForm()
                            pendingDeleteId = null
                            refresh()
                            message = "Connection deleted"
                            busyAction = null
                        }
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun WindowsServiceHints() {
    var services by remember { mutableStateOf(WindowsPostgresServiceHelper.listPostgresServices()) }
    var expandedService by remember { mutableStateOf<String?>(null) }
    if (services.isEmpty()) return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Detected PostgreSQL Windows services (local machine only)")
            services.forEach { svc ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${svc.displayName} — ${svc.status}", modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            expandedService = if (expandedService == svc.name) null else svc.name
                        },
                    ) {
                        Text(if (expandedService == svc.name) "Hide help" else "Start help")
                    }
                }
                if (expandedService == svc.name) {
                    Text(
                        WindowsPostgresServiceHelper.startServiceMessage(svc.name),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun statusLabel(status: ConnectionSessionStatus?): String =
    when (status?.state()) {
        ConnectionLifecycleState.CONNECTED -> "Connected — ${status.message()}"
        ConnectionLifecycleState.CONNECTING -> "Connecting…"
        ConnectionLifecycleState.RECONNECTING -> "Reconnecting…"
        ConnectionLifecycleState.ERROR -> "Error (${status.errorKind().orElse("unknown")}): ${status.message()}"
        ConnectionLifecycleState.DISCONNECTED, null -> "Disconnected"
    }

@Composable
private fun statusColor(status: ConnectionSessionStatus?) =
    when (status?.state()) {
        ConnectionLifecycleState.CONNECTED -> androidx.compose.material3.MaterialTheme.colorScheme.secondary
        ConnectionLifecycleState.ERROR -> androidx.compose.material3.MaterialTheme.colorScheme.error
        ConnectionLifecycleState.CONNECTING, ConnectionLifecycleState.RECONNECTING ->
            androidx.compose.material3.MaterialTheme.colorScheme.primary
        else -> androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    }

private suspend fun testConn(
    facade: ApplicationFacade,
    id: UUID,
    password: String,
    refresh: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val override = if (password.isBlank()) null else password.toCharArray()
    val result = withContext(Dispatchers.IO) { facade.testConnection(id, override) }
    onMessage(if (result.success()) "Test OK: ${result.message()}" else "Test failed (${result.errorKind()}): ${result.message()}")
    refresh()
}

private suspend fun connectConn(
    facade: ApplicationFacade,
    id: UUID,
    password: String,
    onResult: (String, Boolean) -> Unit,
) {
    val override = if (password.isBlank()) null else password.toCharArray()
    val status =
        withContext(Dispatchers.IO) {
            if (override == null) facade.connectConnection(id) else facade.connectConnection(id, override)
        }
    onResult(status.message(), status.state() == ConnectionLifecycleState.CONNECTED)
}

private suspend fun disconnectConn(facade: ApplicationFacade, id: UUID, refresh: () -> Unit, onMessage: (String) -> Unit) {
    val status = withContext(Dispatchers.IO) { facade.disconnectConnection(id) }
    onMessage(status.message())
    refresh()
}

private suspend fun reconnectConn(facade: ApplicationFacade, id: UUID, password: String, refresh: () -> Unit, onMessage: (String) -> Unit) {
    withContext(Dispatchers.IO) { facade.disconnectConnection(id) }
    connectConn(facade, id, password) { msg, _ -> onMessage(msg) }
    refresh()
}
