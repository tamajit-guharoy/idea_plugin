package com.example.demo1;


import com.example.demo1.settings.TestCaseSettings
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.*
import java.io.File
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.text.JTextComponent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class TestCaseWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Close GitHub Copilot Chat window if it's open
        val copilotToolWindow = ToolWindowManager.getInstance(project).getToolWindow("GitHub Copilot Chat")
        copilotToolWindow?.hide()
        
        val testCaseViewer = TestCaseViewer(project)
        val content = ContentFactory.getInstance().createContent(testCaseViewer.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class TestCaseViewer(private val project: Project) {
    private val mainPanel: JPanel
    private val contentArea: EditorTextField
    private val counterLabel: JLabel

    private var currentIndex: Int = 0
    private var codeList = ArrayList<String>()

    init {

        // Create EditorTextField with Java syntax highlighting
        contentArea = EditorTextField(
            "",
            project,
            FileTypeManager.getInstance().getFileTypeByExtension("java")
        ).apply {
            setOneLineMode(false)
            isViewer = true  // Make it read-only

            // Set editor colors and font
            addSettingsProvider { editor ->
                (editor as EditorEx).apply {
                    setVerticalScrollbarVisible(true)
                    setHorizontalScrollbarVisible(true)
                    settings.apply {
                        isLineNumbersShown = true
                        isAutoCodeFoldingEnabled = true
                    }
                    colorsScheme = EditorColorsManager.getInstance().globalScheme
                }
            }
        }

        counterLabel = JLabel("0 of 0")
        mainPanel = createMainPanel()
    }

    fun getContent() = mainPanel

    private fun createMainPanel(): JPanel {
        val panel = JPanel(BorderLayout())

        // Create top panel for search and buttons
        val topPanel = JPanel(BorderLayout())

        // Create search and class input panel
        val inputPanel = JPanel(BorderLayout(10, 0))  // Add horizontal gap between components


        // Create Java Class input panel with Create button
        val javaClassPanel = JPanel(BorderLayout(5, 0))  // Add small gap between label and field
        val javaClassLabel = JLabel("Java Class:")
        val javaClassField = JBTextField(30)
        javaClassField.emptyText.text = "com.example.MyClass"

        // Create a panel for the field and create button
        val classFieldPanel = JPanel(BorderLayout(5, 0))
        classFieldPanel.add(javaClassField, BorderLayout.CENTER)

        val createClassButton = JButton("Create Class")
        createClassButton.addActionListener {
            createJavaClass(javaClassField.text, contentArea.text)
        }
        classFieldPanel.add(createClassButton, BorderLayout.EAST)

        javaClassPanel.add(javaClassLabel, BorderLayout.WEST)
        javaClassPanel.add(classFieldPanel, BorderLayout.CENTER)


        // Add both panels to input panel
        inputPanel.add(javaClassPanel, BorderLayout.WEST)

        // Add input panel to top panel
        topPanel.add(inputPanel, BorderLayout.CENTER)

        // Create buttons panel for the right side
//        val buttonsPanel = JPanel(FlowLayout(FlowLayout.RIGHT))

        // Create print panel button
        val printPanelButton = JButton("Load Test Case")
        printPanelButton.addActionListener {
            loadPanelContent()
        }
//        buttonsPanel.add(printPanelButton)

//        topPanel.add(buttonsPanel, BorderLayout.EAST)

        // Create navigation panel
        val navigationPanel = JPanel(BorderLayout(5, 5))
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 0))

        // Create navigation buttons
        val upButton = JButton("▲ Previous")
        val downButton = JButton("▼ Next")

        upButton.addActionListener {
            if (currentIndex > 0) {
                currentIndex--
                updateContentDisplay()
            }
        }

        downButton.addActionListener {
            if (currentIndex < codeList.size - 1) {
                currentIndex++
                updateContentDisplay()
            }
        }

        buttonPanel.add(upButton)
        buttonPanel.add(downButton)
        buttonPanel.add(printPanelButton)
        navigationPanel.add(buttonPanel, BorderLayout.CENTER)
        navigationPanel.add(counterLabel, BorderLayout.EAST)

        // Create content panel
        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(navigationPanel, BorderLayout.NORTH)
        contentPanel.add(JBScrollPane(contentArea), BorderLayout.CENTER)

        // Add all components to main panel
        panel.add(topPanel, BorderLayout.NORTH)

   /*     // Create split pane for tree and content
        val splitPane = javax.swing.JSplitPane(
            javax.swing.JSplitPane.VERTICAL_SPLIT,
            JBScrollPane(tree),
            contentPanel
        ).apply {
            dividerLocation = 200
        }*/

        panel.add(contentPanel, BorderLayout.CENTER)

        return panel
    }

    private fun createJavaClass(fullClassName: String, content: String) {
        try {
            if (fullClassName.isBlank()) {
                Messages.showErrorDialog(
                    project,
                    "Please enter a valid class name including package (e.g., com.example.MyClass)",
                    "Invalid Class Name"
                )
                return
            }

            // Split the full class name into package and class name
            val lastDot = fullClassName.lastIndexOf('.')
            if (lastDot == -1) {
                Messages.showErrorDialog(
                    project,
                    "Please include a package name (e.g., com.example.MyClass)",
                    "Invalid Class Name"
                )
                return
            }

            val packageName = fullClassName.substring(0, lastDot)
            val className = fullClassName.substring(lastDot + 1)

            // Get class location from settings
            val settings = TestCaseSettings.getInstance(project)
            val sourceRoot = File(project.basePath, settings.classLocation)
            val packagePath = packageName.replace('.', File.separatorChar)
            val packageDir = File(sourceRoot, packagePath)
            packageDir.mkdirs()

            // Create the Java file
            val javaFile = File(packageDir, "$className.java")

            // Check if file already exists
            if (javaFile.exists()) {
                val result = Messages.showYesNoDialog(
                    project,
                    "File ${javaFile.name} already exists. Do you want to overwrite it?",
                    "File Exists",
                    "Overwrite",
                    "Cancel",
                    Messages.getQuestionIcon()
                )
                if (result != Messages.YES) {
                    return
                }
            }

            // Modify content to add package declaration and replace class name
            val modifiedContent = buildString {
                append("package $packageName;\n\n")
                
                // Extract the old class name from the content
                val oldClassNameMatch = "class\\s+(\\w+)\\s*\\{".toRegex().find(content)
                val oldClassName = oldClassNameMatch?.groupValues?.get(1) ?: ""
                
                // Replace all occurrences of the old class name with the new class name
                val contentWithReplacedClassName = content.replace(oldClassName.toRegex(), className)
                
                // Replace the class declaration with the new class name
                val finalContent = contentWithReplacedClassName.replace("class .*?\\s*\\{".toRegex(), "class $className {")
                
                append(finalContent)
            }

            // Write the content to the file
            javaFile.writeText(modifiedContent)

            Messages.showInfoMessage(
                project,
                "Java class created successfully at ${javaFile.absolutePath}",
                "Success"
            )

        } catch (ex: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to create Java class: ${ex.message}",
                "Error Creating Class"
            )
        }
    }

    private fun loadPanelContent() {
        SwingUtilities.invokeLater {
            try {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("GitHub Copilot Chat")
                toolWindow?.show {
//                    getAllJPanelTexts(toolWindow.component)
                    loadCodeBlocks(toolWindow.component)

                    // Set index to last message (size - 1)
                    currentIndex = if (codeList.isNotEmpty()) codeList.size - 1 else 0

                    if (codeList.isNotEmpty()) {
                        println("Loaded ${codeList.size} messages, starting at last message (index $currentIndex)") // Debug log
                        updateContentDisplay()
                        // Close the GitHub Copilot Chat window after loading content
                        SwingUtilities.invokeLater {
                            toolWindow.hide()
                        }
                    } else {
                        Messages.showErrorDialog(
                            project,
                            "No chat messages found",
                            "Error Finding Content"
                        )
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(
                    project,
                    "Failed to get chat content: ${ex.message}",
                    "Error Getting Content"
                )
            }
        }
    }

    private fun loadCodeBlocks(component: Component) {
        codeList = ArrayList<String>()
        // Print all JPanels at the beginning
        if (component is Container) {
            println("\n=== All JPanels in Component ===")
            component.components.forEach { child ->
                if (child is JPanel) {
                    println("Found JPanel: ${child.name ?: "unnamed"} - ${child.javaClass.simpleName}")
                    // Print the text content if any
                    val textComps = mutableListOf<JTextComponent>()
                    findTextComponents(child, textComps)
                    textComps.forEach { textComp ->
                        if (!textComp.javaClass.name.equals("com.github.copilot.chat.message.HtmlContentComponent") && textComp.text?.isNotBlank() == true) {
                            println("  Contains text: ${textComp.text}")
                            //define a list of codeList
                            codeList.add(textComp.text)
                        }
                    }
                }
            }
            println("=== End of JPanels List ===\n")
        }

    }

    private fun findTextComponents(component: Component, textComponents: MutableList<JTextComponent>) {
        when (component) {
            is JTextComponent -> textComponents.add(component)
            is Container -> component.components.forEach { child ->
                findTextComponents(child, textComponents)
            }
        }
    }


    private fun updateContentDisplay() {
        if (codeList.isNotEmpty()) {
            contentArea.text = codeList[currentIndex]
            counterLabel.text = "${currentIndex + 1} of ${codeList.size}"
            println("Displaying message ${currentIndex + 1} of ${codeList.size} (index $currentIndex)") // Debug log
        } else {
            contentArea.text = ""
            counterLabel.text = "0 of 0"
        }
    }

}
