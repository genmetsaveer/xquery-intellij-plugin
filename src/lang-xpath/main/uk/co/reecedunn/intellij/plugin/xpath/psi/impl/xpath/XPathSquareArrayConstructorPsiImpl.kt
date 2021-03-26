/*
 * Copyright (C) 2016-2018, 2020-2021 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xpath.psi.impl.xpath

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import uk.co.reecedunn.intellij.plugin.core.sequences.children
import uk.co.reecedunn.intellij.plugin.xdm.types.XdmArray
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathSquareArrayConstructor
import uk.co.reecedunn.intellij.plugin.xpm.lang.validation.XpmSyntaxValidationElement
import uk.co.reecedunn.intellij.plugin.xpm.optree.expr.XpmExpression

class XPathSquareArrayConstructorPsiImpl(node: ASTNode) :
    ASTWrapperPsiElement(node),
    XPathSquareArrayConstructor,
    XpmSyntaxValidationElement {
    // region XpmConstructableItemExpression

    override val itemTypeClass: Class<*>
        get() = XdmArray::class.java

    override val itemExpression: XpmExpression
        get() = this

    // endregion
    // region XpmArrayExpression

    override val expressionElement: PsiElement
        get() = this

    override val memberExpressions: Sequence<XpmExpression>
        get() = children().filterIsInstance<XpmExpression>()

    // endregion
    // region XpmSyntaxValidationElement

    override val conformanceElement: PsiElement
        get() = firstChild

    // endregion
}
