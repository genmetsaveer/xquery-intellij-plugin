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
package uk.co.reecedunn.intellij.plugin.core.lang.injection

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import java.lang.StringBuilder

class LiteralTextEscaperImpl<T : LiteralTextHost>(host: T) : LiteralTextEscaper<T>(host) {
    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        TODO()
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        TODO()
    }

    override fun getRelevantTextRange(): TextRange = myHost.relevantTextRange

    override fun isOneLine(): Boolean = myHost.isOneLine
}
