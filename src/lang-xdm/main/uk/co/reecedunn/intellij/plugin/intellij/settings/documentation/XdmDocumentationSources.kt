/*
 * Copyright (C) 2019 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.intellij.settings.documentation

import com.intellij.openapi.options.Configurable
import uk.co.reecedunn.intellij.plugin.intellij.resources.XdmBundle
import javax.swing.JComponent
import javax.swing.JPanel

class XdmDocumentationSources : Configurable {
    override fun getDisplayName(): String = XdmBundle.message("settings.document-sources.title")

    override fun createComponent(): JComponent? {
        return JPanel()
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun apply() {
    }

    override fun reset() {
    }
}
