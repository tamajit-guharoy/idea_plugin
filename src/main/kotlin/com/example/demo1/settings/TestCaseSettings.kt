package com.example.demo1.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "JsonViewerSettings",
    storages = [Storage("jsonViewerSettings.xml")]
)
class TestCaseSettings : PersistentStateComponent<TestCaseSettings> {
    var defaultPrompt: String = "create a java method that prints 10 numbers"
    var classLocation: String = "src/main/java"
    var joinPrompt: String = "join the following data"
    var groupReducePrompt: String = "group and reduce the following data"

    override fun getState(): TestCaseSettings = this

    override fun loadState(state: TestCaseSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): TestCaseSettings {
            return project.service<TestCaseSettings>()
        }
    }
} 