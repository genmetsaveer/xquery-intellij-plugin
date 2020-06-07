/*
 * Copyright (C) 2020 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.marklogic.roxy.module

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import uk.co.reecedunn.intellij.plugin.core.vfs.relativePathTo
import uk.co.reecedunn.intellij.plugin.core.vfs.toPsiFile
import uk.co.reecedunn.intellij.plugin.marklogic.roxy.configuration.RoxyConfiguration
import uk.co.reecedunn.intellij.plugin.xdm.context.XstContext
import uk.co.reecedunn.intellij.plugin.xpm.module.loader.XpmModuleLoader
import uk.co.reecedunn.intellij.plugin.xpm.module.loader.XpmModuleLoaderFactory
import uk.co.reecedunn.intellij.plugin.xpm.module.path.XpmModulePath
import uk.co.reecedunn.intellij.plugin.xpm.module.path.impl.XpmModuleLocationPath

class RoxyModuleLoader : XpmModuleLoader {
    // region XpmModuleLoader

    override fun resolve(path: XpmModulePath, context: VirtualFile?): PsiElement? {
        return when (path) {
            is XpmModuleLocationPath -> RoxyConfiguration.getInstance(path.project).modulePaths.map {
                it.findFileByRelativePath(path.path)?.toPsiFile(path.project)
            }.firstOrNull()
            else -> null
        }
    }

    override fun context(path: XpmModulePath, context: VirtualFile?): XstContext? {
        return when (path) {
            is XpmModuleLocationPath -> resolve(path, context) as? XstContext
            else -> null
        }
    }

    override fun relativePathTo(file: VirtualFile, project: Project): String? {
        return RoxyConfiguration.getInstance(project).modulePaths.map {
            it.relativePathTo(file)
        }.firstOrNull()
    }

    // endregion
    // region XpmModuleLoaderFactory

    companion object : XpmModuleLoaderFactory {
        override fun loader(context: String?): XpmModuleLoader? = RoxyModuleLoader()
    }

    // endregion
}
