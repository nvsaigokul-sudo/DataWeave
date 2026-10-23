package com.dataweave.app

import java.awt.BorderLayout
import java.awt.Dialog
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities

/**
 * Compose Desktop surfaces many startup failures as the native "Failed to launch JVM" dialog.
 * Log to disk and show a Java dialog when possible.
 */
object LaunchFailureReporting {
    fun install() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            report("uncaught", e)
        }
    }

    fun report(phase: String, throwable: Throwable) {
        LaunchDiagnostics.appendLaunchLog("FAILURE phase=$phase message=${throwable.message}")
        LaunchDiagnostics.writeLaunchError(throwable)
        System.err.println("DataWeave startup failed ($phase): ${throwable.message}")
        throwable.printStackTrace(System.err)
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater {
                    showErrorDialog(phase, throwable)
                }
            }
        } catch (_: Exception) {
            // Headless or AWT unavailable.
        }
    }

    private fun showErrorDialog(phase: String, throwable: Throwable) {
        val dialog =
            JDialog(null as java.awt.Frame?, "DataWeave startup failed", Dialog.ModalityType.APPLICATION_MODAL)
        dialog.layout = BorderLayout()
        val area =
            JTextArea(
                buildString {
                    appendLine("Phase: $phase")
                    appendLine("Log: ${LaunchDiagnostics.launchLogPath().parent.resolve("launch-error.log")}")
                    appendLine()
                    appendLine(throwable.stackTraceToString())
                },
            )
        area.isEditable = false
        dialog.add(JScrollPane(area), BorderLayout.CENTER)
        val close = JButton("Close")
        close.addActionListener { dialog.dispose() }
        dialog.add(close, BorderLayout.SOUTH)
        dialog.setSize(720, 480)
        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true
    }
}
