/*
 * Copyright (C) 2016-2017 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xquery.psi.impl.xquery

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import uk.co.reecedunn.intellij.plugin.core.data.CachedProperty
import uk.co.reecedunn.intellij.plugin.core.sequences.children
import uk.co.reecedunn.intellij.plugin.core.sequences.filterNotToken
import uk.co.reecedunn.intellij.plugin.xdm.XsUntyped
import uk.co.reecedunn.intellij.plugin.xdm.model.XdmAtomicValue
import uk.co.reecedunn.intellij.plugin.xdm.model.XdmSequenceType
import uk.co.reecedunn.intellij.plugin.xdm.model.XdmSimpleExpression
import uk.co.reecedunn.intellij.plugin.xquery.ast.xquery.XQueryPostfixExpr
import uk.co.reecedunn.intellij.plugin.xquery.parser.XQueryElementType

class XQueryPostfixExprPsiImpl(node: ASTNode):
        ASTWrapperPsiElement(node),
        XQueryPostfixExpr,
        XdmSimpleExpression {

    override fun subtreeChanged() {
        super.subtreeChanged()
        staticEval.invalidate()
    }

    /**
     * Perform static evaluation on the PostfixExpr to determine the static type and value.
     */
    private val staticEval: CachedProperty<Pair<XdmSequenceType, Any?>?> = CachedProperty {
        val children = children().filterNotToken(XQueryElementType.WHITESPACE_OR_COMMENT).iterator()
        if (!children.hasNext())
            null
        else {
            val value = children.next() as? XdmAtomicValue
            if (value == null || children.hasNext())
                null
            else // Literal without a Predicate, ArgumentList, or Lookup expression.
                Pair(value.staticType, value.lexicalRepresentation)
        }
    }

    override val staticType get(): XdmSequenceType = staticEval.get()?.first ?: XsUntyped

    override val constantValue get(): Any? = staticEval.get()?.second
}
