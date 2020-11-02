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
package uk.co.reecedunn.intellij.plugin.xpath.ast

import com.intellij.psi.PsiElement
import uk.co.reecedunn.intellij.plugin.core.sequences.children
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathExpr

fun <T> PsiElement.filterExpressions(klass: Class<T>): Sequence<T> {
    val item = children().filterIsInstance(klass)
    val sequence = children().filterIsInstance<XPathExpr>().firstOrNull()
    return if (sequence != null)
        sequenceOf(item, sequence.children().filterIsInstance(klass)).flatten()
    else
        item
}

inline fun <reified T> PsiElement.filterExpressions(): Sequence<T> = filterExpressions(T::class.java)