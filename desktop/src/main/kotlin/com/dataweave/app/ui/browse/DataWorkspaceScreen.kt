package com.dataweave.app.ui.browse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.backend.ApplicationFacade
import com.dataweave.backend.domain.TableRef
import java.util.UUID

data class DataTab(val id: String, val table: TableRef)

@Composable
fun DataWorkspaceScreen(
    facade: ApplicationFacade,
    connectionId: UUID?,
    selectedTable: TableRef?,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { mutableStateListOf<DataTab>() }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTable, connectionId) {
        val table = selectedTable ?: return@LaunchedEffect
        if (connectionId == null) return@LaunchedEffect
        val existing = tabs.indexOfFirst { it.table == table }
        if (existing < 0) {
            tabs.add(DataTab("${table.schema}.${table.name}", table))
            selectedTab = tabs.lastIndex
        } else {
            selectedTab = existing
        }
    }

    if (connectionId == null || tabs.isEmpty()) {
        Text(
            "Connect and pick a table or view from the explorer.",
            modifier = modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        return
    }

    Column(modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = index == selectedTab,
                    onClick = { selectedTab = index },
                    text = { Text(tab.table.name) },
                )
            }
        }
        val tab = tabs.getOrNull(selectedTab) ?: return
        SpreadsheetDataGrid(facade, connectionId, tab.table, Modifier.weight(1f))
    }
}
