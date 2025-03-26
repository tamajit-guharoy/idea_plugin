package com.example.demo1

import com.example.demo1.settings.TestCaseSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import java.awt.Component
import java.awt.Container
import java.awt.Robot
import java.awt.event.KeyEvent
import javax.swing.SwingUtilities

class GenerateTestCase : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (isJsonInResources(virtualFile.path)) {
            SwingUtilities.invokeLater {
                try {
                    // Open the file first
                    FileEditorManager.getInstance(project).openFile(virtualFile, true)

                    // Show Copilot Chat window
                    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("GitHub Copilot Chat")
                    toolWindow?.show {
                        // Wait for window to be ready
                        Thread.sleep(1000)

                        // Find and click the Add reference button
                        val robot = Robot()
                        val content = toolWindow.contentManager.getContent(0)
                        val component = content?.component

                        if (component != null) {
                            println(virtualFile)

                            // Get default prompt from application-level settings
                            val settings =
                                ApplicationManager.getApplication().getService(TestCaseSettings::class.java)
                            val defaultPrompt = settings.defaultPrompt
                            println("Default prompt from settings: $defaultPrompt")

                            // Find the input text field
                            val textComponents = mutableListOf<javax.swing.text.JTextComponent>()
                            findTextComponents(component, textComponents)

                            // Get the last editable text component (usually the input field)
                            val inputField = textComponents.findLast { it.isEditable }

                            if (inputField != null) {
                                // Set focus to the input field
                                inputField.requestFocus()
                                Thread.sleep(100)  // Wait for focus

                                // Set the text
                                inputField.text = defaultPrompt

                                // Press Enter to submit
                                val robot = Robot()
                                robot.keyPress(KeyEvent.VK_ENTER)
                                robot.keyRelease(KeyEvent.VK_ENTER)

                                // Wait a bit to ensure the message is sent
                                Thread.sleep(500)
                            } else {
                                println("Could not find input field in Copilot Chat")
                            }

                            //   findAndClickAddReferenceButton(component, robot)
                        }
                    }
                } catch (ex: Exception) {
                    Messages.showErrorDialog(
                        project,
                        "Failed to add reference: ${ex.message}",
                        "Error Adding Reference"
                    )
                }
            }
        }
    }

    private fun findTextComponents(component: Component, textComponents: MutableList<javax.swing.text.JTextComponent>) {
        when (component) {
            is javax.swing.text.JTextComponent -> textComponents.add(component)
            is Container -> component.components.forEach { child ->
                findTextComponents(child, textComponents)
            }
        }
    }
    private fun isJsonInResources(path: String): Boolean {
        return path.contains("src/main/resources") &&
                path.endsWith(".json")
    }

}