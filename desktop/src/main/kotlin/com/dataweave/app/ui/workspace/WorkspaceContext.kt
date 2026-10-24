package com.dataweave.app.ui.workspace

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dataweave.app.preferences.UserPreferences
import com.dataweave.backend.domain.TableRef
import java.util.UUID

/** Shared workspace selection state (FR-021) for explorer, Data, SQL, and Datasets. */
class WorkspaceContext {
    var activeConnectionId by mutableStateOf(
        UserPreferences.loadLastConnectionId()?.let { runCatching { UUID.fromString(it) }.getOrNull() },
    )
        private set

    var activeDatabase by mutableStateOf<String?>(null)
        private set

    var selectedSchema by mutableStateOf("public")
        private set

    var selectedTable by mutableStateOf<TableRef?>(null)
        private set

    var statusMessage by mutableStateOf("Ready")
        private set

    fun setActiveConnection(id: UUID?) {
        activeConnectionId = id
        UserPreferences.saveLastConnectionId(id?.toString())
        if (id == null) {
            activeDatabase = null
        }
    }

    fun updateActiveDatabase(database: String?) {
        activeDatabase = database
    }

    fun selectTable(table: TableRef) {
        selectedSchema = table.schema
        selectedTable = table
        statusMessage = "Selected ${table.qualifiedName()}"
    }

    fun updateStatus(message: String) {
        statusMessage = message
    }
}
