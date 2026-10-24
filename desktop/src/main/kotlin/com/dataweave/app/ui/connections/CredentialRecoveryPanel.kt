package com.dataweave.app.ui.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dataweave.app.ui.theme.DwSpacing
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.ConnectionLifecycleState
import com.dataweave.backend.domain.ConnectionProfile
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Inline recovery when a saved profile exists but the vault has no password (upgrade, vault miss, or first connect).
 */
@Composable
fun CredentialRecoveryPanel(
    facade: ApplicationFacade,
    profile: ConnectionProfile,
    onConnected: (UUID) -> Unit,
    onOpenFullConnections: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Card(modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(DwSpacing.md).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Restore connection credentials", style = MaterialTheme.typography.titleSmall)
            Text(
                "DataWeave could not read a saved password for \"${profile.name}\". " +
                    "Re-enter your PostgreSQL password below. It is stored in the OS credential vault, not in the profile file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(profile.name, {}, readOnly = true, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                "${profile.host}:${profile.port}",
                {},
                readOnly = true,
                label = { Text("Host") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(profile.databaseName, {}, readOnly = true, label = { Text("Database") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(profile.username, {}, readOnly = true, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                password,
                { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (it.startsWith("Test OK") || it.startsWith("Connected")) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !busy && password.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            busy = true
                            message = null
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    facade.updateConnection(
                                        profile.id,
                                        profile.name,
                                        profile.host,
                                        profile.port,
                                        profile.databaseName,
                                        profile.username,
                                        password.toCharArray(),
                                        profile.sslMode,
                                        profile.connectTimeoutSeconds,
                                    )
                                }
                            }.onSuccess {
                                message = "Credentials saved securely"
                            }.onFailure {
                                message = it.message ?: "Save failed"
                            }
                            busy = false
                        }
                    },
                ) { Text(if (busy) "Saving…" else "Save credentials") }
                OutlinedButton(
                    enabled = !busy && password.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            busy = true
                            val test =
                                withContext(Dispatchers.IO) {
                                    facade.testConnection(profile.id, password.toCharArray())
                                }
                            message =
                                if (test.success()) "Test OK: ${test.message()}" else "Test failed: ${test.message()}"
                            busy = false
                        }
                    },
                ) { Text("Test") }
                Button(
                    enabled = !busy && password.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            busy = true
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    facade.updateConnection(
                                        profile.id,
                                        profile.name,
                                        profile.host,
                                        profile.port,
                                        profile.databaseName,
                                        profile.username,
                                        password.toCharArray(),
                                        profile.sslMode,
                                        profile.connectTimeoutSeconds,
                                    )
                                    facade.connectConnection(profile.id)
                                }
                            }.onSuccess { st ->
                                password = ""
                                message = st.message()
                                if (st.state() == ConnectionLifecycleState.CONNECTED) {
                                    onConnected(profile.id)
                                }
                            }.onFailure {
                                message = it.message ?: "Connect failed"
                            }
                            busy = false
                        }
                    },
                ) { Text("Connect") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenFullConnections) { Text("Open Connections") }
                onDismiss?.let { dismiss ->
                    OutlinedButton(onClick = dismiss) { Text("Cancel") }
                }
            }
        }
    }
}
