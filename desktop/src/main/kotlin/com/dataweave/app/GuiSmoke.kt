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
    System.setProperty("skiko.renderApi", "SOFTWARE")
    System.setProperty("java.awt.headless", "false")
    java.util.Timer(true).schedule(
        object : java.util.TimerTask() {
            override fun run() {
                System.err.println("GUI_SMOKE_TIMEOUT")
                Runtime.getRuntime().halt(2)
            }
        },
        120_000L,
    )
    LaunchDiagnostics.appendLaunchLog("gui-smoke: starting DesktopSession")
    DesktopSession.start("--dataweave.test.mode=true").use { session ->
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
                System.out.flush()
                LaunchDiagnostics.appendLaunchLog("gui-smoke: GUI_SMOKE_OK")
                delay(500)
                exitApplication()
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("GUI smoke")
            }
        }
    }
    }
}
