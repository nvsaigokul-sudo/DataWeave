package com.dataweave.app.ui.connections

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionProfile
import com.dataweave.backend.domain.SslMode
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ConnectionsScreen(
    facade: ApplicationFacade,
    onConnected: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    var connections by remember { mutableStateOf<List<ConnectionProfile>>(emptyList()) }
    var name by remember { mutableStateOf("Local PostgreSQL") }
    var host by remember { mutableStateOf("127.0.0.1") }
    var port by remember { mutableStateOf("5432") }
    var database by remember { mutableStateOf("postgres") }
    var username by remember { mutableStateOf("postgres") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            connections = withContext(Dispatchers.IO) { facade.listConnections() }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PostgreSQL connections")
        Text(
            "Passwords are stored in the OS credential vault (Windows Credential Manager, macOS Keychain, or Linux Secret Service when available).",
        )
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
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) {
                                        facade.saveConnection(
                                            name,
                                            host,
                                            port.toIntOrNull() ?: 5432,
                                            database,
                                            username,
                                            password.toCharArray(),
                                            SslMode.PREFER,
                                            10,
                                        )
                                    }
                                }.onSuccess {
                                    password = ""
                                    message = "Saved connection ${it.name}"
                                    refresh()
                                }.onFailure { message = it.message }
                            }
                        },
                    ) { Text("Save") }
                }
                message?.let { Text(it) }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(connections, key = { it.id }) { conn ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(conn.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                            Text("${conn.username}@${conn.host}:${conn.port}/${conn.databaseName}")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) { facade.testConnection(conn.id, null) }
                                    message = if (result.success) "OK: ${result.message}" else "Failed (${result.errorKind}): ${result.message}"
                                    if (result.success) onConnected(conn.id)
                                }
                            },
                        ) { Text("Test & browse") }
                        Button(
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) { facade.deleteConnection(conn.id) }
                                    refresh()
                                }
                            },
                        ) { Text("Delete") }
                    }
                }
            }
        }
    }
}
