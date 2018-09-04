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

package uk.co.reecedunn.intellij.plugin.xpath.tests.model

import com.intellij.psi.PsiElement
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
import uk.co.reecedunn.intellij.plugin.core.data.CachingBehaviour
import uk.co.reecedunn.intellij.plugin.core.sequences.children
import uk.co.reecedunn.intellij.plugin.core.tests.assertion.assertThat
import uk.co.reecedunn.intellij.plugin.xdm.*
import uk.co.reecedunn.intellij.plugin.xdm.datatype.QName
import uk.co.reecedunn.intellij.plugin.xdm.model.*
import uk.co.reecedunn.intellij.plugin.xpath.ast.plugin.PluginQuantifiedExprBinding
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.*
import uk.co.reecedunn.intellij.plugin.xpath.model.*
import uk.co.reecedunn.intellij.plugin.xpath.model.XPathTypeDeclaration
import uk.co.reecedunn.intellij.plugin.xquery.tests.parser.ParserTestCase
import java.math.BigDecimal
import java.math.BigInteger

// NOTE: This class is private so the JUnit 4 test runner does not run the tests contained in it.
@Suppress("UNCHECKED_CAST")
private class XPathModelTest : ParserTestCase() {
    // region Lexical Values
    // region BracedUriLiteral (XdmStaticValue)

    @Test
    fun testBracedUriLiteral() {
        val literal = parse<XPathBracedURILiteral>("Q{http://www.example.com\uFFFF}")[0] as XdmStaticValue
        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(literal.staticValue as String, `is`("http://www.example.com\uFFFF"))
        assertThat(literal.staticType, `is`(XsAnyURI))

        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testBracedUriLiteral_Unclosed() {
        val literal = parse<XPathBracedURILiteral>("Q{http://www.example.com")[0] as XdmStaticValue
        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(literal.staticValue as String, `is`("http://www.example.com"))
        assertThat(literal.staticType, `is`(XsAnyURI))

        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region DoubleLiteral (XdmStaticValue)

    @Test
    fun testDoubleLiteral() {
        val literal = parse<XPathDoubleLiteral>("1e3")[0] as XdmStaticValue
        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(literal.staticValue as Double, `is`(1e3))
        assertThat(literal.staticType, `is`(XsDouble))

        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region StringLiteral (XdmStaticValue)

    @Test
    fun testStringLiteral() {
        val literal = parse<XPathStringLiteral>("\"Lorem ipsum.\uFFFF\"")[0] as XdmStaticValue
        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(literal.staticValue as String, `is`("Lorem ipsum.\uFFFF")) // U+FFFF = BAD_CHARACTER token.
        assertThat(literal.staticType, `is`(XsString))

        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testStringLiteral_Unclosed() {
        val literal = parse<XPathStringLiteral>("\"Lorem ipsum.")[0] as XdmStaticValue
        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(literal.staticValue as String, `is`("Lorem ipsum."))
        assertThat(literal.staticType, `is`(XsString))

        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testStringLiteral_EscapeApos() {
        val literal = parse<XPathStringLiteral>("'''\"\"'")[0] as XdmStaticValue
        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(literal.staticValue as String, `is`("'\"\""))
        assertThat(literal.staticType, `is`(XsString))

        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testStringLiteral_EscapeQuot() {
        val literal = parse<XPathStringLiteral>("\"''\"\"\"")[0] as XdmStaticValue
        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(literal.staticValue as String, `is`("''\""))
        assertThat(literal.staticType, `is`(XsString))

        assertThat(literal.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // endregion
    // region Lexical and Expanded QNames
    // region NCName (XdmStaticValue)

    @Test
    fun testNCName() {
        val expr = parse<XPathNCName>("test")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(true))
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(qname.toString(), `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testNCName_Keyword() {
        val expr = parse<XPathNCName>("option")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(true))
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("option"))

        assertThat(qname.toString(), `is`("option"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testNCName_Wildcard() {
        val expr = parse<XPathNCName>("declare option * \"\";")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.staticValue, `is`(nullValue()))
        assertThat(expr.staticType, `is`(XsUntyped))
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region QName (XdmStaticValue)

    @Test
    fun testQName() {
        val expr = parse<XPathQName>("fn:true")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(true))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("fn"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("true"))

        assertThat(qname.toString(), `is`("fn:true"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testQName_KeywordPrefix() {
        val expr = parse<XPathQName>("option:test")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(true))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("option"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(qname.toString(), `is`("option:test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testQName_KeywordLocalName() {
        val expr = parse<XPathQName>("test:case")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(true))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("test"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("case"))

        assertThat(qname.toString(), `is`("test:case"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testQName_NoLocalName() {
        val expr = parse<XPathQName>("xs:")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.staticValue, `is`(nullValue()))
        assertThat(expr.staticType, `is`(XsUntyped))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region URIQualifiedName (XdmStaticValue)

    @Test
    fun testURIQualifiedName() {
        val expr = parse<XPathURIQualifiedName>("Q{http://www.example.com}test")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(false))
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(qname.toString(), `is`("Q{http://www.example.com}test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testURIQualifiedName_EmptyNamespace() {
        val expr = parse<XPathURIQualifiedName>("Q{}test")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(false))
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`(""))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(qname.toString(), `is`("Q{}test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testURIQualifiedName_KeywordLocalName() {
        val expr = parse<XPathURIQualifiedName>("Q{http://www.example.com}option")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.staticValue, `is`(instanceOf(QName::class.java)))
        assertThat(expr.staticType, `is`(XsQName))

        val qname = expr.staticValue as QName
        assertThat(qname.isLexicalQName, `is`(false))
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(expr))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("option"))

        assertThat(qname.toString(), `is`("Q{http://www.example.com}option"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testURIQualifiedName_NoLocalName() {
        val expr = parse<XPathURIQualifiedName>("Q{http://www.example.com}")[0] as XdmStaticValue
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.staticValue, `is`(nullValue()))
        assertThat(expr.staticType, `is`(XsUntyped))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // endregion
    // region Sequence Types
    // region AtomicOrUnionType (XPathTypeDeclaration)

    @Test
    fun testAtomicOrUnionType_NCName() {
        val expr = parse<XPathAtomicOrUnionType>("\$x instance of test")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathNCName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testAtomicOrUnionType_QName() {
        val expr = parse<XPathAtomicOrUnionType>("\$x instance of a:type")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathQName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("type"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testAtomicOrUnionType_URIQualifiedName() {
        val expr = parse<XPathAtomicOrUnionType>("\$x instance of Q{http://www.example.com}test")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testAtomicOrUnionType_BuiltinAtomicType() {
        val expr = parse<XPathAtomicOrUnionType>("\$x instance of Q{http://www.w3.org/2001/XMLSchema}boolean")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsBoolean))
        assertThat(type.baseType, `is`(XsAnyAtomicType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("boolean"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testAtomicOrUnionType_BuiltinUnionType() {
        val expr = parse<XPathAtomicOrUnionType>("\$x instance of Q{http://www.w3.org/2001/XMLSchema}numeric")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNumeric))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("numeric"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testAtomicOrUnionType_BuiltinListType() {
        // NOTE: XQuery processors (e.g. BaseX and MarkLogic) allow these in cast expressions,
        // due to them being referenced in XMLSchema, but report errors elsewhere.

        val expr = parse<XPathAtomicOrUnionType>("\$x instance of Q{http://www.w3.org/2001/XMLSchema}NMTOKENS")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNMTOKENS))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("NMTOKENS"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testAtomicOrUnionType_BuiltinAbstractType() {
        // NOTE: Errors when using these types are detected and reported elsewhere.

        val expr = parse<XPathAtomicOrUnionType>("\$x instance of Q{http://www.w3.org/2001/XMLSchema}anyType")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsAnyType))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("anyType"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region TypeName (XPathTypeDeclaration)

    @Test
    fun testTypeName_NCName() {
        val expr = parse<XPathTypeName>("\$x instance of element(*, test)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathNCName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testTypeName_QName() {
        val expr = parse<XPathTypeName>("\$x instance of element(*, a:type)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathQName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("type"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testTypeName_URIQualifiedName() {
        val expr = parse<XPathTypeName>("\$x instance of element(*, Q{http://www.example.com}test)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testTypeName_BuiltinAtomicType() {
        val expr = parse<XPathTypeName>("\$x instance of element(*, Q{http://www.w3.org/2001/XMLSchema}boolean)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsBoolean))
        assertThat(type.baseType, `is`(XsAnyAtomicType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("boolean"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testTypeName_BuiltinUnionType() {
        val expr = parse<XPathTypeName>("\$x instance of element(*, Q{http://www.w3.org/2001/XMLSchema}numeric)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNumeric))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("numeric"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testTypeName_BuiltinListType() {
        // NOTE: XQuery processors (e.g. BaseX and MarkLogic) allow these in cast expressions,
        // due to them being referenced in XMLSchema, but report errors elsewhere.

        val expr = parse<XPathTypeName>("\$x instance of element(*, Q{http://www.w3.org/2001/XMLSchema}NMTOKENS)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNMTOKENS))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("NMTOKENS"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testTypeName_BuiltinAbstractType() {
        val expr = parse<XPathTypeName>("\$x instance of element(*, Q{http://www.w3.org/2001/XMLSchema}anyType)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsAnyType))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("anyType"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region PITest (XPathTypeDeclaration)

    @Test
    fun testPITest() {
        val expr = parse<XPathPITest>("\$x instance of processing-instruction()")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        val type = expr.declaredType as? XdmProcessingInstruction
        assertThat(type, `is`(notNullValue()))
        assertThat(type!!.nodeName, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testPITest_NCNameTarget_NCName() {
        val expr = parse<XPathPITest>("\$x instance of processing-instruction(test)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        val type = expr.declaredType as? XdmProcessingInstruction
        assertThat(type, `is`(notNullValue()))

        assertThat(type!!.nodeName!!.prefix, `is`(nullValue()))
        assertThat(type.nodeName!!.namespace, `is`(nullValue()))
        assertThat(type.nodeName!!.localName.staticValue as String, `is`("test"))
        assertThat(type.nodeName!!.declaration!!.get(), `is`(instanceOf(XPathNCName::class.java)))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testPITest_NCNameTarget_Keyword() {
        val expr = parse<XPathPITest>("\$x instance of processing-instruction(option)")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        val type = expr.declaredType as? XdmProcessingInstruction
        assertThat(type, `is`(notNullValue()))

        assertThat(type!!.nodeName!!.prefix, `is`(nullValue()))
        assertThat(type.nodeName!!.namespace, `is`(nullValue()))
        assertThat(type.nodeName!!.localName.staticValue as String, `is`("option"))
        assertThat(type.nodeName!!.declaration!!.get(), `is`(instanceOf(XPathNCName::class.java)))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testPITest_StringLiteralTarget_NCName() {
        val expr = parse<XPathPITest>("\$x instance of processing-instruction('test')")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        val type = expr.declaredType as? XdmProcessingInstruction
        assertThat(type, `is`(notNullValue()))

        // TODO: Support StringLiteral encoded NCName PITarget specifiers.
        assertThat(type!!.nodeName, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testPITest_StringLiteralTarget_Keyword() {
        val expr = parse<XPathPITest>("\$x instance of processing-instruction('option')")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        val type = expr.declaredType as? XdmProcessingInstruction
        assertThat(type, `is`(notNullValue()))

        // TODO: Support StringLiteral encoded NCName PITarget specifiers.
        assertThat(type!!.nodeName, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testPITest_StringLiteralTarget_NormalizeSpace() {
        val expr = parse<XPathPITest>("\$x instance of processing-instruction('  test\t\n\r\r  ')")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        val type = expr.declaredType as? XdmProcessingInstruction
        assertThat(type, `is`(notNullValue()))

        // TODO: Support StringLiteral encoded NCName PITarget specifiers.
        assertThat(type!!.nodeName, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testPITest_StringLiteralTarget_InvalidNCName() {
        val expr = parse<XPathPITest>("\$x instance of processing-instruction('*')")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        val type = expr.declaredType as? XdmProcessingInstruction
        assertThat(type, `is`(notNullValue()))

        // TODO: Support StringLiteral encoded NCName PITarget specifiers.
        assertThat(type!!.nodeName, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region CommentTest (XPathTypeDeclaration)

    @Test
    fun testCommentTest() {
        val expr = parse<XPathCommentTest>("\$x instance of comment()")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.declaredType, `is`(XdmComment))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region TextTest (XPathTypeDeclaration)

    @Test
    fun testTextTest() {
        val expr = parse<XPathTextTest>("\$x instance of text()")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.declaredType, `is`(XdmText))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region NamespaceNodeTest (XPathTypeDeclaration)

    @Test
    fun testNamespaceNodeTest() {
        val expr = parse<XPathNamespaceNodeTest>("\$x instance of namespace-node()")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.declaredType, `is`(XdmNamespace))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region AnyKindTest (XPathTypeDeclaration)

    @Test
    fun testAnyKindTest() {
        val expr = parse<XPathAnyKindTest>("\$x instance of node()")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.declaredType, `is`(XdmNode))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // endregion
    // region Single Types
    // region SimpleTypeName (XPathTypeDeclaration)

    @Test
    fun testSimpleTypeName_NCName() {
        val expr = parse<XPathSimpleTypeName>("\$x cast as test")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathNCName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testSimpleTypeName_QName() {
        val expr = parse<XPathSimpleTypeName>("\$x cast as a:type")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathQName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("type"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testSimpleTypeName_URIQualifiedName() {
        val expr = parse<XPathSimpleTypeName>("\$x cast as Q{http://www.example.com}test")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSimpleTypeName_BuiltinAtomicType() {
        val expr = parse<XPathSimpleTypeName>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}boolean")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsBoolean))
        assertThat(type.baseType, `is`(XsAnyAtomicType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("boolean"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSimpleTypeName_BuiltinUnionType() {
        val expr = parse<XPathSimpleTypeName>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}numeric")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNumeric))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("numeric"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSimpleTypeName_BuiltinListType() {
        // NOTE: XQuery processors (e.g. BaseX and MarkLogic) allow these in cast expressions,
        // due to them being referenced in XMLSchema, but report errors elsewhere.

        val expr = parse<XPathSimpleTypeName>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}NMTOKENS")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNMTOKENS))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("NMTOKENS"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSimpleTypeName_BuiltinAbstractType() {
        val expr = parse<XPathSimpleTypeName>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}anyType")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsAnyType))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("anyType"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region SingleType (XPathTypeDeclaration)

    @Test
    fun testSingleType_NCName() {
        val expr = parse<XPathSingleType>("\$x cast as test")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathNCName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testSingleType_QName() {
        val expr = parse<XPathSingleType>("\$x cast as a:type")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathQName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("type"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.DoNotCache))
    }

    @Test
    fun testSingleType_URIQualifiedName() {
        val expr = parse<XPathSingleType>("\$x cast as Q{http://www.example.com}test")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        val type = expr.declaredType as XdmSimpleType
        assertThat(type.typeName, `is`(notNullValue()))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.itemType, `is`(type))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("test"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSingleType_BuiltinAtomicType() {
        val expr = parse<XPathSingleType>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}boolean")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsBoolean))
        assertThat(type.baseType, `is`(XsAnyAtomicType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("boolean"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSingleType_BuiltinUnionType() {
        val expr = parse<XPathSingleType>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}numeric")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNumeric))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ONE))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("numeric"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSingleType_BuiltinListType() {
        // NOTE: XQuery processors (e.g. BaseX and MarkLogic) allow these in cast expressions,
        // due to them being referenced in XMLSchema, but report errors elsewhere.

        val expr = parse<XPathSingleType>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}NMTOKENS")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsNMTOKENS))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("NMTOKENS"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSingleType_BuiltinAbstractType() {
        val expr = parse<XPathSingleType>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}anyType")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmSimpleType::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmSimpleType
        assertThat(type.itemType, `is`(XsAnyType))
        assertThat(type.baseType, `is`(XsAnySimpleType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.MANY))

        val qname = type.typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("anyType"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testSingleType_Optional() {
        val expr = parse<XPathSingleType>("\$x cast as Q{http://www.w3.org/2001/XMLSchema}boolean?")[0] as XPathTypeDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        val name = (expr as PsiElement).firstChild.firstChild as XPathURIQualifiedName
        assertThat(expr.declaredType, `is`(instanceOf(XdmOptional::class.java)))

        // NOTE: itemType is not `this`, but is the mapped builtin type object.
        val type = expr.declaredType as XdmOptional
        assertThat(type.itemType, `is`(instanceOf(XdmSimpleType::class.java)))
        assertThat(type.itemType.itemType, `is`(XsBoolean))
        assertThat((type.itemType as XdmSimpleType).baseType, `is`(XsAnyAtomicType))
        assertThat(type.lowerBound, `is`(XdmSequenceType.Occurs.ZERO))
        assertThat(type.upperBound, `is`(XdmSequenceType.Occurs.ONE))

        val qname = (type.itemType as XdmSimpleType).typeName!!
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.w3.org/2001/XMLSchema"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("boolean"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // endregion
    // region Variables
    // region Param (XPathVariableBinding)

    @Test
    fun testParam_NCName() {
        val expr = parse<XPathParam>("function (\$x) {}")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        val name = (expr as PsiElement).firstChild.nextSibling as XPathNCName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testParam_QName() {
        val expr = parse<XPathParam>("function (\$a:x) {}")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))
        assertThat(expr.variableName, `is`(notNullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        val name = (expr as PsiElement).firstChild.nextSibling as XPathQName

        val qname = expr.variableName as QName
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testParam_URIQualifiedName() {
        val expr = parse<XPathParam>(
                "function (\$Q{http://www.example.com}x) {}")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        val name = (expr as PsiElement).firstChild.nextSibling as XPathURIQualifiedName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testParam_MissingVarName() {
        val expr = parse<XPathParam>("function (\$) {}")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(nullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region QuantifiedExprBinding (XPathVariableBinding)

    @Test
    fun testQuantifiedExprBinding_NCName() {
        val expr = parse<PluginQuantifiedExprBinding>("some \$x in \$y satisfies \$z")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        val varname = (expr as PsiElement).children().filterIsInstance<XPathVarName>().first()
        val name = varname.firstChild as XPathNCName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testQuantifiedExprBinding_QName() {
        val expr = parse<PluginQuantifiedExprBinding>("some \$a:x in \$a:y satisfies \$a:z")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))
        assertThat(expr.variableName, `is`(notNullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        val varname = (expr as PsiElement).children().filterIsInstance<XPathVarName>().first()
        val name = varname.firstChild as XPathQName

        val qname = expr.variableName as QName
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testQuantifiedExprBinding_URIQualifiedName() {
        val expr = parse<PluginQuantifiedExprBinding>(
                "some \$Q{http://www.example.com}x in  \$Q{http://www.example.com}y satisfies \$Q{http://www.example.com}z")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        val varname = (expr as PsiElement).children().filterIsInstance<XPathVarName>().first()
        val name = varname.firstChild as XPathURIQualifiedName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testQuantifiedExprBinding_MissingVarName() {
        val expr = parse<PluginQuantifiedExprBinding>("some \$")[0] as XPathVariableBinding
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(nullValue()))
        assertThat(expr.variableType, `is`(nullValue()))
        assertThat(expr.variableValue, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region VarName (XPathVariableName)

    @Test
    fun testVarName_NCName() {
        val expr = parse<XPathVarName>("let \$x := 2 return \$y")[0] as XPathVariableName
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))

        val name = (expr as PsiElement).firstChild as XPathNCName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testVarName_QName() {
        val expr = parse<XPathVarName>("let \$a:x := 2 return \$a:y")[0] as XPathVariableName
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))
        assertThat(expr.variableName, `is`(notNullValue()))

        val name = (expr as PsiElement).firstChild as XPathQName

        val qname = expr.variableName as QName
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testVarName_URIQualifiedName() {
        val expr = parse<XPathVarName>("let \$Q{http://www.example.com}x := 2 return \$Q{http://www.example.com}y")[0] as XPathVariableName
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))

        val name = (expr as PsiElement).firstChild as XPathURIQualifiedName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region VarRef (XPathVariableReference)

    @Test
    fun testVarRef_NCName() {
        val expr = parse<XPathVarRef>("let \$x := 2 return \$y")[0] as XPathVariableReference
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))

        val varname = (expr as PsiElement).children().filterIsInstance<XPathVarName>().first()
        val name = varname.firstChild as XPathNCName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("y"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testVarRef_QName() {
        val expr = parse<XPathVarRef>("let \$a:x := 2 return \$a:y")[0] as XPathVariableReference
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))
        assertThat(expr.variableName, `is`(notNullValue()))

        val varname = (expr as PsiElement).children().filterIsInstance<XPathVarName>().first()
        val name = varname.firstChild as XPathQName

        val qname = expr.variableName as QName
        assertThat(qname.namespace, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.prefix?.staticType, `is`(XsNCName))
        assertThat(qname.prefix?.staticValue as String, `is`("a"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("y"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testVarRef_URIQualifiedName() {
        val expr = parse<XPathVarRef>("let \$Q{http://www.example.com}x := 2 return \$Q{http://www.example.com}y")[0] as XPathVariableReference
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(notNullValue()))

        val varname = (expr as PsiElement).children().filterIsInstance<XPathVarName>().first()
        val name = varname.firstChild as XPathURIQualifiedName

        val qname = expr.variableName as QName
        assertThat(qname.prefix, `is`(nullValue()))
        assertThat(qname.declaration?.get(), `is`(name))

        assertThat(qname.namespace?.staticType, `is`(XsAnyURI))
        assertThat(qname.namespace?.staticValue as String, `is`("http://www.example.com"))

        assertThat(qname.localName.staticType, `is`(XsNCName))
        assertThat(qname.localName.staticValue as String, `is`("y"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testVarRef_MissingVarName() {
        val expr = parse<XPathVarRef>("let \$x := 2 return \$")[0] as XPathVariableReference
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
        assertThat(expr.variableName, `is`(nullValue()))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // endregion
    // region Functions
    // region ParamList (XPathFunctionArguments)

    @Test
    fun testParamList_SingleArguments() {
        val expr = parse<XPathParamList>("function (\$x) {}")[0] as XPathFunctionArguments<XPathVariableBinding>
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.arity, `is`(1))
        assertThat(expr.arguments.size, `is`(1))

        assertThat(expr.arguments[0].variableType, `is`(nullValue()))
        assertThat(expr.arguments[0].variableValue, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.namespace, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.prefix, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testParamList_MultipleArguments() {
        val expr = parse<XPathParamList>("function (\$x, \$y) {}")[0] as XPathFunctionArguments<XPathVariableBinding>
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.arity, `is`(2))
        assertThat(expr.arguments.size, `is`(2))

        assertThat(expr.arguments[0].variableType, `is`(nullValue()))
        assertThat(expr.arguments[0].variableValue, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.namespace, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.prefix, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.localName.staticValue as String, `is`("x"))

        assertThat(expr.arguments[1].variableType, `is`(nullValue()))
        assertThat(expr.arguments[1].variableValue, `is`(nullValue()))
        assertThat(expr.arguments[1].variableName!!.namespace, `is`(nullValue()))
        assertThat(expr.arguments[1].variableName!!.prefix, `is`(nullValue()))
        assertThat(expr.arguments[1].variableName!!.localName.staticValue as String, `is`("y"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // region InlineFunctionExpr (XPathFunctionDeclaration)

    @Test
    fun testInlineFunctionExpr_NoArguments() {
        val expr = parse<XPathInlineFunctionExpr>("function () {}")[0] as XPathFunctionDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))

        assertThat(expr.functionName, `is`(nullValue()))
        assertThat(expr.returnType, `is`(nullValue()))
        assertThat(expr.arity, `is`(0))
        assertThat(expr.arguments.size, `is`(0))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testInlineFunctionExpr_SingleArguments() {
        val expr = parse<XPathInlineFunctionExpr>("function (\$x) {}")[0] as XPathFunctionDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.functionName, `is`(nullValue()))
        assertThat(expr.returnType, `is`(nullValue()))
        assertThat(expr.arity, `is`(1))
        assertThat(expr.arguments.size, `is`(1))

        assertThat(expr.arguments[0].variableType, `is`(nullValue()))
        assertThat(expr.arguments[0].variableValue, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.namespace, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.prefix, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.localName.staticValue as String, `is`("x"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    @Test
    fun testInlineFunctionExpr_MultipleArguments() {
        val expr = parse<XPathInlineFunctionExpr>("function (\$x, \$y) {}")[0] as XPathFunctionDeclaration
        assertThat(expr.cacheable, `is`(CachingBehaviour.Undecided))

        assertThat(expr.functionName, `is`(nullValue()))
        assertThat(expr.returnType, `is`(nullValue()))
        assertThat(expr.arity, `is`(2))
        assertThat(expr.arguments.size, `is`(2))

        assertThat(expr.arguments[0].variableType, `is`(nullValue()))
        assertThat(expr.arguments[0].variableValue, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.namespace, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.prefix, `is`(nullValue()))
        assertThat(expr.arguments[0].variableName!!.localName.staticValue as String, `is`("x"))

        assertThat(expr.arguments[1].variableType, `is`(nullValue()))
        assertThat(expr.arguments[1].variableValue, `is`(nullValue()))
        assertThat(expr.arguments[1].variableName!!.namespace, `is`(nullValue()))
        assertThat(expr.arguments[1].variableName!!.prefix, `is`(nullValue()))
        assertThat(expr.arguments[1].variableName!!.localName.staticValue as String, `is`("y"))

        assertThat(expr.cacheable, `is`(CachingBehaviour.Cache))
    }

    // endregion
    // endregion
}
