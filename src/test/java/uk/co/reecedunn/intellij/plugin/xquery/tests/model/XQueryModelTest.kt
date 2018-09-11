/*
 * Copyright (C) 2017-2018 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xquery.tests.model

import com.intellij.psi.PsiElement
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
import uk.co.reecedunn.intellij.plugin.core.data.CachingBehaviour
import uk.co.reecedunn.intellij.plugin.core.sequences.children
import uk.co.reecedunn.intellij.plugin.core.tests.assertion.assertThat
import uk.co.reecedunn.intellij.plugin.xdm.XsAnyURI
import uk.co.reecedunn.intellij.plugin.xdm.XsNCName
import uk.co.reecedunn.intellij.plugin.xdm.datatype.QName
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathNCName
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathQName
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathURIQualifiedName
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPathVarName
import uk.co.reecedunn.intellij.plugin.xpath.model.XPathVariableBinding
import uk.co.reecedunn.intellij.plugin.xpath.model.XPathVariableDeclaration
import uk.co.reecedunn.intellij.plugin.xpath.model.XPathVariableName
import uk.co.reecedunn.intellij.plugin.xquery.ast.plugin.PluginBlockVarDeclEntry
import uk.co.reecedunn.intellij.plugin.xquery.ast.plugin.PluginDefaultCaseClause
import uk.co.reecedunn.intellij.plugin.xquery.ast.xquery.*
import uk.co.reecedunn.intellij.plugin.xquery.tests.parser.ParserTestCase

// NOTE: This class is private so the JUnit 4 test runner does not run the tests contained in it.
private class XQueryModelTest : ParserTestCase() {
    // region Variables
    // region BlockVarDeclEntry (XPathVariableDeclaration) [XQuery Scripting Extensions]

    @Test
    fun testBlockVarDeclEntry_NCName() {
        val expr = parse<PluginBlockVarDeclEntry>("block { declare \$x := \$y; 2 }")[0] as XPathVariableDeclaration

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testBlockVarDeclEntry_QName() {
        val expr = parse<PluginBlockVarDeclEntry>("block { declare \$a:x := \$a:y; 2 }")[0] as XPathVariableDeclaration

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testBlockVarDeclEntry_URIQualifiedName() {
        val expr = parse<PluginBlockVarDeclEntry>(
            "block { declare \$Q{http://www.example.com}x := \$Q{http://www.example.com}y; 2 }"
        )[0] as XPathVariableDeclaration

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testBlockVarDeclEntry_MissingVarName() {
        val expr = parse<PluginBlockVarDeclEntry>("block { declare \$ := \$y; 2 }")[0] as XPathVariableDeclaration
        assertThat(expr.variableName, `is`(nullValue()))
    }

    @Test
    fun testBlockVarDeclEntry_Multiple_NCName() {
        val decls = parse<PluginBlockVarDeclEntry>("block { declare \$x := 1, \$y := 2; 3 }")
        assertThat(decls.size, `is`(2))

        val expr = decls[1] as XPathVariableDeclaration

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    // endregion
    // region CaseClause (XPathVariableBinding)

    @Test
    fun testCaseClause_NCName() {
        val expr = parse<XQueryCaseClause>(
            "typeswitch (\$x) case \$y as xs:string return \$z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testCaseClause_QName() {
        val expr = parse<XQueryCaseClause>(
            "typeswitch (\$a:x) case \$a:y as xs:string return \$a:z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testCaseClause_URIQualifiedName() {
        val expr = parse<XQueryCaseClause>(
            "typeswitch (\$Q{http://www.example.com}x) " +
            "case \$Q{http://www.example.com}y as xs:string " +
            "return \$Q{http://www.example.com}z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testCaseClause_NoVarName() {
        val expr = parse<XQueryCaseClause>("typeswitch (\$x) case xs:string return \$z")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region CountClause (XPathVariableBinding)

    @Test
    fun testCountClause_NCName() {
        val expr = parse<XQueryCountClause>("for \$x in \$y count \$z return \$w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testCountClause_QName() {
        val expr = parse<XQueryCountClause>("for \$a:x in \$a:y count \$a:z return \$a:w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testCountClause_URIQualifiedName() {
        val expr = parse<XQueryCountClause>(
            "for \$Q{http://www.example.com}x in \$Q{http://www.example.com}y count \$Q{http://www.example.com}z " +
            "return \$Q{http://www.example.com}w"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testCountClause_MissingVarName() {
        val expr = parse<XQueryCountClause>("for \$x in \$y count \$")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region CurrentItem (XPathVariableBinding)

    @Test
    fun testCurrentItem_NCName() {
        val expr = parse<XQueryCurrentItem>("for sliding window \$x in \$y start \$w when true() return \$z")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    @Test
    fun testCurrentItem_QName() {
        val expr = parse<XQueryCurrentItem>("for sliding window \$a:x in \$a:y start \$a:w when true() return \$a:z")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    @Test
    fun testCurrentItem_URIQualifiedName() {
        val expr = parse<XQueryCurrentItem>(
            "for sliding window \$Q{http://www.example.com}x in \$Q{http://www.example.com}y " +
            "start \$Q{http://www.example.com}w when true() " +
            "return \$Q{http://www.example.com}z"
        )[0] as XPathVariableBinding

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    // endregion
    // region DefaultCaseClause (XPathVariableBinding)

    @Test
    fun testDefaultCaseClause_NCName() {
        val expr = parse<PluginDefaultCaseClause>("typeswitch (\$x) default \$y return \$z")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testDefaultCaseClause_QName() {
        val expr = parse<PluginDefaultCaseClause>(
            "typeswitch (\$a:x) default \$a:y return \$a:z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testDefaultCaseClause_URIQualifiedName() {
        val expr = parse<PluginDefaultCaseClause>(
            "typeswitch (\$Q{http://www.example.com}x) " +
            "default \$Q{http://www.example.com}y " +
            "return \$Q{http://www.example.com}z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testDefaultCaseClause_NoVarName() {
        val expr = parse<PluginDefaultCaseClause>("typeswitch (\$x) default return \$z")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region ForBinding (XPathVariableBinding)

    @Test
    fun testForBinding_NCName() {
        val expr = parse<XQueryForBinding>("for \$x at \$y in \$z return \$w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testForBinding_QName() {
        val expr = parse<XQueryForBinding>("for \$a:x at \$a:y in \$a:z return \$a:w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testForBinding_URIQualifiedName() {
        val expr = parse<XQueryForBinding>(
            "for \$Q{http://www.example.com}x at \$Q{http://www.example.com}y in \$Q{http://www.example.com}z " +
            "return \$Q{http://www.example.com}w"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testForBinding_MissingVarName() {
        val expr = parse<XQueryForBinding>("for \$ \$y return \$w")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region GroupingSpec (XPathVariableBinding)

    @Test
    fun testGroupingSpec_NCName() {
        val expr = parse<XQueryGroupingSpec>("for \$x in \$y group by \$z return \$w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testGroupingSpec_QName() {
        val expr = parse<XQueryGroupingSpec>(
            "for \$a:x in \$a:y group by \$a:z return \$a:w"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testGroupingSpec_URIQualifiedName() {
        val expr = parse<XQueryGroupingSpec>(
            "for \$Q{http://www.example.com}x in \$Q{http://www.example.com}y " +
            "group by \$Q{http://www.example.com}z " +
            "return \$Q{http://www.example.com}w"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testGroupingSpec_MissingVarName() {
        val expr = parse<XQueryGroupingSpec>("for \$x in \$y group by \$")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region GroupingVariable (XPathVariableName)

    @Test
    fun testGroupingVariable_NCName() {
        val expr = parse<XQueryGroupingVariable>("for \$x in \$y group by \$z return \$w")[0] as XPathVariableName

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testGroupingVariable_QName() {
        val expr = parse<XQueryGroupingVariable>(
            "for \$a:x in \$a:y group by \$a:z return \$a:w"
        )[0] as XPathVariableName

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testGroupingVariable_URIQualifiedName() {
        val expr = parse<XQueryGroupingVariable>(
            "for \$Q{http://www.example.com}x in \$Q{http://www.example.com}y " +
            "group by \$Q{http://www.example.com}z " +
            "return \$Q{http://www.example.com}w"
        )[0] as XPathVariableName

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("z"))
    }

    @Test
    fun testGroupingVariable_MissingVarName() {
        val expr = parse<XQueryGroupingVariable>("for \$x in \$y group by \$")[0] as XPathVariableName
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region LetBinding (XPathVariableBinding)

    @Test
    fun testLetBinding_NCName() {
        val expr = parse<XQueryLetBinding>("let \$x := 2 return \$w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testLetBinding_QName() {
        val expr = parse<XQueryLetBinding>("let \$a:x := 2 return \$a:w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testLetBinding_URIQualifiedName() {
        val expr = parse<XQueryLetBinding>(
            "let \$Q{http://www.example.com}x := 2 return \$Q{http://www.example.com}w"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testLetBinding_MissingVarName() {
        val expr = parse<XQueryLetBinding>("let \$ := 2 return \$w")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region NextItem (XPathVariableBinding)

    @Test
    fun testNextItem_NCName() {
        val expr = parse<XQueryNextItem>("for sliding window \$x in \$y start \$v next \$w when true() return \$z")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    @Test
    fun testNextItem_QName() {
        val expr = parse<XQueryNextItem>(
            "for sliding window \$a:x in \$a:y start \$a:v next \$a:w when true() return \$a:z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    @Test
    fun testNextItem_URIQualifiedName() {
        val expr = parse<XQueryNextItem>(
            "for sliding window \$Q{http://www.example.com}x in \$Q{http://www.example.com}y " +
            "start \$Q{http://www.example.com}v next \$Q{http://www.example.com}w when true() " +
            "return \$Q{http://www.example.com}z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    // endregion
    // region PositionalVar (XPathVariableBinding)

    @Test
    fun testPositionalVar_NCName() {
        val expr = parse<XQueryPositionalVar>("for \$x at \$y in \$z return \$w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testPositionalVar_QName() {
        val expr = parse<XQueryPositionalVar>("for \$a:x at \$a:y in \$a:z return \$a:w")[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testPositionalVar_URIQualifiedName() {
        val expr = parse<XQueryPositionalVar>(
            "for \$Q{http://www.example.com}x at \$Q{http://www.example.com}y in \$Q{http://www.example.com}z " +
            "return \$Q{http://www.example.com}w"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("y"))
    }

    @Test
    fun testPositionalVar_MissingVarName() {
        val expr = parse<XQueryPositionalVar>("for \$x at \$ \$z return \$w")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region PreviousItem (XPathVariableBinding)

    @Test
    fun testPreviousItem_NCName() {
        val expr = parse<XQueryPreviousItem>(
            "for sliding window \$x in \$y start \$v previous \$w when true() return \$z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    @Test
    fun testPreviousItem_QName() {
        val expr = parse<XQueryPreviousItem>(
            "for sliding window \$a:x in \$a:y start \$a:v previous \$a:w when true() return \$a:z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    @Test
    fun testPreviousItem_URIQualifiedName() {
        val expr = parse<XQueryPreviousItem>(
            "for sliding window \$Q{http://www.example.com}x in \$Q{http://www.example.com}y " +
            "start \$Q{http://www.example.com}v previous \$Q{http://www.example.com}w when true() " +
            "return \$Q{http://www.example.com}z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("w"))
    }

    // endregion
    // region SlidingWindowClause (XPathVariableBinding)

    @Test
    fun testSlidingWindowClause_NCName() {
        val expr = parse<XQuerySlidingWindowClause>(
            "for sliding window \$x in \$y return \$z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testSlidingWindowClause_QName() {
        val expr = parse<XQuerySlidingWindowClause>(
            "for sliding window \$a:x in \$a:y return \$a:z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testSlidingWindowClause_URIQualifiedName() {
        val expr = parse<XQuerySlidingWindowClause>(
            "for sliding window \$Q{http://www.example.com}x in \$Q{http://www.example.com}y " +
            "return \$Q{http://www.example.com}z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testSlidingWindowClause_MissingVarName() {
        val expr = parse<XQuerySlidingWindowClause>("for sliding window \$ \$y return \$w")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region TumblingWindowClause (XPathVariableBinding)

    @Test
    fun testTumblingWindowClause_NCName() {
        val expr = parse<XQueryTumblingWindowClause>(
            "for tumbling window \$x in \$y return \$z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testTumblingWindowClause_QName() {
        val expr = parse<XQueryTumblingWindowClause>(
            "for tumbling window \$a:x in \$a:y return \$a:z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testTumblingWindowClause_URIQualifiedName() {
        val expr = parse<XQueryTumblingWindowClause>(
            "for tumbling window \$Q{http://www.example.com}x in \$Q{http://www.example.com}y " +
            "return \$Q{http://www.example.com}z"
        )[0] as XPathVariableBinding

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testTumblingWindowClause_MissingVarName() {
        val expr = parse<XQueryTumblingWindowClause>("for tumbling window \$ \$y return \$w")[0] as XPathVariableBinding
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // region VarDecl (XPathVariableDeclaration)

    @Test
    fun testVarDecl_NCName() {
        val expr = parse<XQueryVarDecl>("declare variable \$x := \$y;")[0] as XPathVariableDeclaration

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testVarDecl_QName() {
        val expr = parse<XQueryVarDecl>("declare variable \$a:x := \$a:y;")[0] as XPathVariableDeclaration

        val qname = expr.variableName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.prefix!!.data, `is`("a"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testVarDecl_URIQualifiedName() {
        val expr = parse<XQueryVarDecl>(
            "declare variable \$Q{http://www.example.com}x := \$Q{http://www.example.com}y;"
        )[0] as XPathVariableDeclaration

        val qname = expr.variableName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace!!.data, `is`("http://www.example.com"))
        assertThat(qname.localName!!.data, `is`("x"))
    }

    @Test
    fun testVarDecl_MissingVarName() {
        val expr = parse<XQueryVarDecl>("declare variable \$ := \$y;")[0] as XPathVariableDeclaration
        assertThat(expr.variableName, `is`(nullValue()))
    }

    // endregion
    // endregion
}
