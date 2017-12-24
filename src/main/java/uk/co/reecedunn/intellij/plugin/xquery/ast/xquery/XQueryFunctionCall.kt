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
package uk.co.reecedunn.intellij.plugin.xquery.ast.xquery

import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathPrimaryExpr
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathEQName

/**
 * An XQuery 1.0 `FunctionCall` node in the XQuery AST.
 */
interface XQueryFunctionCall : XPathPrimaryExpr {
    val functionName: XPathEQName?

    val arity: Int
}
