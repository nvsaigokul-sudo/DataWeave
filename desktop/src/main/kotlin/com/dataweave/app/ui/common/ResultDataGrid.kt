package com.dataweave.app.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dataweave.backend.domain.DataPage

@Composable
fun ResultDataGrid(page: DataPage, modifier: Modifier = Modifier) {
    val hScroll = rememberScrollState()
    Column(modifier) {
        Text(
            "${page.rows.size} rows · ${page.orderingDescription}",
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(Modifier.horizontalScroll(hScroll)) {
            Column {
                Row(Modifier.fillMaxWidth()) {
                    page.columns.forEach { col ->
                        Text(col.name, modifier = Modifier.padding(8.dp))
                    }
                }
                LazyColumn {
                    items(page.rows) { row ->
                        Row {
                            page.columns.forEach { col ->
                                Text(
                                    row[col.name]?.toString() ?: "",
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultDataCard(page: DataPage?, error: String?, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        when {
            error != null -> Text("Error: $error", modifier = Modifier.padding(12.dp))
            page == null -> Text("No results yet.", modifier = Modifier.padding(12.dp))
            else -> ResultDataGrid(page, Modifier.padding(12.dp))
        }
    }
}
