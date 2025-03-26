package com.example.demo1.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class TestCaseSettingsConfigurable(private val project: Project) : Configurable {
    private var mainPanel: JPanel? = null
    private var promptField: JBTextField? = null

    override fun getDisplayName(): String = "JSON Viewer Settings"

    override fun createComponent(): JComponent {
        promptField = JBTextField()
        
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Default Copilot Prompt: "), promptField!!, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = TestCaseSettings.getInstance(project)
        return promptField?.text != settings.defaultPrompt
    }

    override fun apply() {
        val settings = TestCaseSettings.getInstance(project)
        settings.defaultPrompt = promptField?.text ?: settings.defaultPrompt
    }

    override fun reset() {
        val settings = TestCaseSettings.getInstance(project)
        promptField?.text = settings.defaultPrompt
    }

    override fun disposeUIResources() {
        mainPanel = null
        promptField = null
    }
} 