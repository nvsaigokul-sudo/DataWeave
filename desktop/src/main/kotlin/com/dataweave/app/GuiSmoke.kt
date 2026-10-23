package com.dataweave.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.dataweave.backend.DesktopSession
import kotlinx.coroutines.delay

/** Packaged-app smoke: Spring + Compose window (AWT/Skiko), without user interaction. */
fun runGuiSmoke() {
    LaunchDiagnostics.appendLaunchLog("gui-smoke: starting DesktopSession")
    val session =
        DesktopSession.start(
            "--dataweave.test.mode=true",
        )
    Runtime.getRuntime().addShutdownHook(Thread { session.close() })
    LaunchDiagnostics.appendLaunchLog("gui-smoke: opening Compose window")
    application {
        Window(
            onCloseRequest = {
                session.close()
                exitApplication()
            },
            title = "DataWeave GUI Smoke",
            state = rememberWindowState(width = 480.dp, height = 320.dp),
        ) {
            LaunchedEffect(Unit) {
                println("GUI_SMOKE_OK")
                LaunchDiagnostics.appendLaunchLog("gui-smoke: GUI_SMOKE_OK")
                delay(1500)
                session.close()
                exitApplication()
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("GUI smoke")
            }
        }
    }
}
