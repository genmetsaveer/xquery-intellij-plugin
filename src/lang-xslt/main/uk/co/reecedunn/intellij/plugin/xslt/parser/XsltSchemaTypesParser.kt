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
package uk.co.reecedunn.intellij.plugin.xslt.parser

import com.intellij.lang.PsiBuilder
import uk.co.reecedunn.intellij.plugin.xdm.psi.tree.ISchemaType
import uk.co.reecedunn.intellij.plugin.xpath.intellij.resources.XPathBundle
import uk.co.reecedunn.intellij.plugin.xpath.parser.XPathParser
import uk.co.reecedunn.intellij.plugin.xslt.intellij.lang.XsltSchemaTypes

class XsltSchemaTypesParser(private val schemaType: ISchemaType) : XPathParser() {
    // region Grammar

    override fun parse(builder: PsiBuilder, isFirst: Boolean): Boolean {
        if (parseSchemaType(builder)) {
            return true
        }
        if (isFirst && !schemaType.allowEmpty) {
            builder.error(XPathBundle.message("parser.error.expected", schemaType.type))
        }
        return false
    }

    private fun parseSchemaType(builder: PsiBuilder): Boolean = when (schemaType) {
        XsltSchemaTypes.Expression -> parseExpr(builder, null)
        XsltSchemaTypes.ItemType -> parseItemType(builder)
        XsltSchemaTypes.Pattern -> parseExpr(builder, null)
        XsltSchemaTypes.QName -> parseQNameOrWildcard(builder, QNAME) != null
        XsltSchemaTypes.SequenceType -> parseSequenceType(builder)
        else -> false
    }

    // endregion
}
