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
package uk.co.reecedunn.intellij.plugin.intellij.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

class XQueryReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return false
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        return false
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        return Access.ReadWrite
    }

    override fun getExpressionAccess(expression: PsiElement): Access {
        return Access.ReadWrite
    }
}
