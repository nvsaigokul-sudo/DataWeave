package com.dataweave.app.ui

import androidx.compose.runtime.Composable
import com.dataweave.app.ui.workspace.WorkspaceShell
import com.dataweave.backend.ApplicationFacade

@Composable
fun AppShell(facade: ApplicationFacade) {
    WorkspaceShell(facade)
}
