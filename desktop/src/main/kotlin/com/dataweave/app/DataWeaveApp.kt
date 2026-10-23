package com.dataweave.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.dataweave.app.ui.AppShell
import com.dataweave.backend.DesktopSession

fun main(args: Array<String>) {
    if (args.contains("--headless-smoke")) {
        runHeadlessSmoke()
        return
    }
    val session = DesktopSession.start(*buildSpringArgs(args))
    Runtime.getRuntime().addShutdownHook(Thread { session.close() })

    application {
        Window(
            onCloseRequest = {
                session.close()
                exitApplication()
            },
            title = "DataWeave",
            state = rememberWindowState(width = 1280.dp, height = 860.dp),
        ) {
            AppShell(session.facade())
        }
    }
}

private fun buildSpringArgs(args: Array<String>): Array<String> {
    if (!args.contains("--dev")) {
        return emptyArray()
    }
    val embedded = DevEmbeddedPostgres.start()
    Runtime.getRuntime().addShutdownHook(Thread { embedded.close() })
    return arrayOf(
        "--dataweave.dev.embedded-pg.port=${embedded.port}",
    )
}
