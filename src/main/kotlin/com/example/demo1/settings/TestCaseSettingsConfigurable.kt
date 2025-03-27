package com.example.demo1.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

class TestCaseSettingsConfigurable(private val project: Project) : Configurable {
    private var mainPanel: JPanel? = null
    private var promptArea: JTextArea? = null
    private var classLocationField: JBTextField? = null
    private var joinPromptArea: JTextArea? = null
    private var groupReducePromptArea: JTextArea? = null

    override fun getDisplayName(): String = "JSON Viewer Settings"

    override fun createComponent(): JComponent {
        promptArea = JTextArea(5, 40)
        promptArea?.lineWrap = true
        promptArea?.wrapStyleWord = true
        
        classLocationField = JBTextField("src/main/java")
        
        joinPromptArea = JTextArea(5, 40)
        joinPromptArea?.lineWrap = true
        joinPromptArea?.wrapStyleWord = true
        
        groupReducePromptArea = JTextArea(5, 40)
        groupReducePromptArea?.lineWrap = true
        groupReducePromptArea?.wrapStyleWord = true
        
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Class Location: "), classLocationField!!, 1, false)
            .addLabeledComponent(JBLabel("Transformation Prompt: "), JBScrollPane(promptArea!!), 1, false)
            .addLabeledComponent(JBLabel("Join Prompt: "), JBScrollPane(joinPromptArea!!), 1, false)
            .addLabeledComponent(JBLabel("Group Reduce Prompt: "), JBScrollPane(groupReducePromptArea!!), 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return mainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = TestCaseSettings.getInstance(project)
        return promptArea?.text != settings.defaultPrompt ||
               classLocationField?.text != settings.classLocation ||
               joinPromptArea?.text != settings.joinPrompt ||
               groupReducePromptArea?.text != settings.groupReducePrompt
    }

    override fun apply() {
        val settings = TestCaseSettings.getInstance(project)
        settings.defaultPrompt = promptArea?.text ?: settings.defaultPrompt
        settings.classLocation = classLocationField?.text ?: settings.classLocation
        settings.joinPrompt = joinPromptArea?.text ?: settings.joinPrompt
        settings.groupReducePrompt = groupReducePromptArea?.text ?: settings.groupReducePrompt
    }

    override fun reset() {
        val settings = TestCaseSettings.getInstance(project)
        promptArea?.text = settings.defaultPrompt
        classLocationField?.text = settings.classLocation
        joinPromptArea?.text = settings.joinPrompt
        groupReducePromptArea?.text = settings.groupReducePrompt
    }

    override fun disposeUIResources() {
        mainPanel = null
        promptArea = null
        classLocationField = null
        joinPromptArea = null
        groupReducePromptArea = null
    }
} 