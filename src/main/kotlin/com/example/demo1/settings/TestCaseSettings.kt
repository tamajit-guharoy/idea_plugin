package com.example.demo1.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "JsonViewerSettings",
    storages = [Storage("jsonViewerSettings.xml")]
)
class TestCaseSettings : PersistentStateComponent<TestCaseSettings> {
    var defaultPrompt: String = loadPromptFromFile("/prompts/default_prompt.txt")
    var classLocation: String = "src/main/java"
    var joinPrompt: String = loadPromptFromFile("/prompts/join_prompt.txt")
    var groupReducePrompt: String = loadPromptFromFile("/prompts/group_reduce_prompt.txt")

    override fun getState(): TestCaseSettings = this

    override fun loadState(state: TestCaseSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        private fun loadPromptFromFile(resourcePath: String): String {
            return try {
                TestCaseSettings::class.java.getResourceAsStream(resourcePath)?.bufferedReader()?.use { 
                    it.readText().trim() 
                } ?: "Enter prompt here"
            } catch (e: Exception) {
                println("Error loading prompt from $resourcePath: ${e.message}")
                "Enter prompt here"
            }
        }

        fun getInstance(project: Project): TestCaseSettings {
            return project.service<TestCaseSettings>()
        }
    }
} 