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
package uk.co.reecedunn.intellij.plugin.saxon.query.s9api.debugger

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XStackFrame
import uk.co.reecedunn.intellij.plugin.core.data.CacheableProperty
import uk.co.reecedunn.intellij.plugin.saxon.query.s9api.binding.trace.InstructionInfo

class SaxonInstructionFrame(val instruction: InstructionInfo, private val query: VirtualFile) : XStackFrame() {
    private fun findFileByPath(path: String?): VirtualFile? {
        return if (path == null)
            query
        else
            query.findFileByRelativePath(path)
    }

    private val sourcePosition = CacheableProperty {
        val file = findFileByPath(instruction.getSystemId())
        val line = instruction.getLineNumber().let { if (it == -1) 1 else it } - 1
        val column = instruction.getColumnNumber().let { if (it == -1) 1 else it } - 1
        XDebuggerUtil.getInstance().createPosition(file, line, column)
    }

    override fun getSourcePosition(): XSourcePosition? {
        return sourcePosition.get()
    }
}