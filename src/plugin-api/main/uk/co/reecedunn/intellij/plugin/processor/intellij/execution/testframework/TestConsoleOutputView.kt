/*
 * Copyright (C) 2021 Reece H. Dunn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.reecedunn.intellij.plugin.processor.intellij.execution.testframework

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl
import com.intellij.openapi.fileTypes.FileTypeEditorHighlighterProviders
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.content.Content
import uk.co.reecedunn.intellij.plugin.core.execution.ui.ContentProvider
import uk.co.reecedunn.intellij.plugin.core.execution.ui.TextConsoleView
import uk.co.reecedunn.intellij.plugin.processor.intellij.execution.process.QueryProcessHandlerBase
import uk.co.reecedunn.intellij.plugin.processor.intellij.execution.process.QueryResultListener
import uk.co.reecedunn.intellij.plugin.processor.intellij.execution.process.QueryResultTime
import uk.co.reecedunn.intellij.plugin.processor.query.QueryResult
import uk.co.reecedunn.intellij.plugin.processor.test.TestFormat
import uk.co.reecedunn.intellij.plugin.xdm.types.XsDurationValue

class TestConsoleOutputView(project: Project, private val outputFormat: TestFormat) :
    TextConsoleView(project),
    ContentProvider,
    QueryResultListener {
    // region ContentProvider

    private var queryProcessHandler: QueryProcessHandlerBase? = null

    override val contentId: String = "TestConsoleOutput"

    override fun getContent(ui: RunnerLayoutUi): Content {
        val consoleTitle = outputFormat.name
        val content = ui.createContent(contentId, component, consoleTitle, AllIcons.Modules.Output, null)
        content.isCloseable = false
        return content
    }

    override fun createRunnerLayoutActions(): Array<AnAction> = arrayOf()

    override fun attachToProcess(processHandler: ProcessHandler) {
        queryProcessHandler = (processHandler as? QueryProcessHandlerBase)
        queryProcessHandler?.addQueryResultListener(this)
    }

    override fun attachToConsole(consoleView: ConsoleView) {
    }

    // endregion
    // region Disposable

    override fun dispose() {
        queryProcessHandler?.removeQueryResultListener(this)
    }

    // endregion
    // region QueryResultListener

    private var psiFile: PsiFile? = null

    override fun onBeginResults() {
        psiFile = null
        clear()
    }

    override fun onEndResults(): PsiFile? {
        val doc = editor?.document ?: return null
        val language = outputFormat.language

        language.associatedFileType!!.let {
            val provider = FileTypeEditorHighlighterProviders.INSTANCE.forFileType(it)
            editor!!.highlighter = provider.getEditorHighlighter(project, it, null, editor!!.colorsScheme)
        }

        psiFile = PsiFileFactory.getInstance(project).createFileFromText(language, doc.text) ?: return null
        psiFile!!.viewProvider.virtualFile.putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, doc)
        return psiFile
    }

    override fun onQueryResult(result: QueryResult) {
        print(result.value as String, ConsoleViewContentType.NORMAL_OUTPUT)
    }

    override fun onException(e: Throwable) {
    }

    override fun onQueryResultTime(resultTime: QueryResultTime, time: XsDurationValue) {
    }

    override fun onQueryResultsPsiFile(psiFile: PsiFile) {
    }

    // endregion
}