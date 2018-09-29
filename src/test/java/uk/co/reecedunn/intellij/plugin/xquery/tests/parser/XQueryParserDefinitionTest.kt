/*
 * Copyright (C) 2016-2018 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xquery.tests.parser

import com.intellij.lang.ParserDefinition
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.co.reecedunn.intellij.plugin.core.lexer.CombinedLexer
import uk.co.reecedunn.intellij.plugin.core.tests.assertion.assertThat
import uk.co.reecedunn.intellij.plugin.core.tests.lang.MockASTNode
import uk.co.reecedunn.intellij.plugin.xqdoc.lexer.XQDocTokenType
import uk.co.reecedunn.intellij.plugin.intellij.fileTypes.XQueryFileType
import uk.co.reecedunn.intellij.plugin.xquery.lexer.XQueryTokenType
import uk.co.reecedunn.intellij.plugin.xquery.parser.XQueryElementType
import uk.co.reecedunn.intellij.plugin.xquery.parser.XQueryParserDefinition
import uk.co.reecedunn.intellij.plugin.xquery.parser.XQueryPsiParser
import uk.co.reecedunn.intellij.plugin.xquery.psi.impl.xquery.XQueryModuleImpl

// NOTE: This class is private so the JUnit 4 test runner does not run the tests contained in it.
private class XQueryParserDefinitionTest : ParserTestCase() {
    @Test
    fun testLexer() {
        val parserDefinition = XQueryParserDefinition()
        assertThat(parserDefinition.createLexer(myProject).javaClass.name, `is`(CombinedLexer::class.java.name))
    }

    @Test
    fun testParser() {
        val parserDefinition = XQueryParserDefinition()
        assertThat(parserDefinition.createParser(myProject).javaClass.name, `is`(XQueryPsiParser::class.java.name))
    }

    @Test
    fun testFileNodeType() {
        val parserDefinition = XQueryParserDefinition()
        assertThat(parserDefinition.fileNodeType, `is`(XQueryElementType.FILE))
    }

    @Test
    fun testWhitespaceTokens() {
        val parserDefinition = XQueryParserDefinition()
        val tokens = parserDefinition.whitespaceTokens
        assertThat(tokens.types.size, `is`(0))
    }

    @Test
    fun testCommentTokens() {
        val parserDefinition = XQueryParserDefinition()
        val tokens = parserDefinition.commentTokens
        assertThat(tokens.types.size, `is`(4))
        assertThat(tokens.contains(XQDocTokenType.COMMENT_CONTENTS), `is`(true))
        assertThat(tokens.contains(XQDocTokenType.CONTENTS), `is`(true))
        assertThat(tokens.contains(XQueryTokenType.COMMENT), `is`(true))
        assertThat(tokens.contains(XQueryTokenType.XML_COMMENT), `is`(true))
    }

    @Test
    fun testStringLiteralElements() {
        val parserDefinition = XQueryParserDefinition()
        val tokens = parserDefinition.stringLiteralElements
        assertThat(tokens.types.size, `is`(4))
        assertThat(tokens.contains(XQueryTokenType.STRING_LITERAL_CONTENTS), `is`(true))
        assertThat(tokens.contains(XQueryTokenType.STRING_CONSTRUCTOR_CONTENTS), `is`(true))
        assertThat(tokens.contains(XQueryTokenType.XML_ATTRIBUTE_VALUE_CONTENTS), `is`(true))
        assertThat(tokens.contains(XQueryTokenType.XML_ELEMENT_CONTENTS), `is`(true))
    }

    @Test
    fun testCreateElement() {
        val parserDefinition = XQueryParserDefinition()

        // foreign ASTNode
        val e = Assertions.assertThrows(AssertionError::class.java) { parserDefinition.createElement(MockASTNode(XQueryTokenType.INTEGER_LITERAL)) }
        assertThat(e.message, `is`("Alien element type [XQUERY_INTEGER_LITERAL_TOKEN]. Can't create XQuery PsiElement for that."))
    }

    @Test
    fun testCreateFile() {
        val parserDefinition = XQueryParserDefinition()
        val file = createVirtualFile("test.xqy", "")
        val psiFile = parserDefinition.createFile(getFileViewProvider(myProject, file, false))
        assertThat(psiFile.javaClass.name, `is`(XQueryModuleImpl::class.java.name))
        assertThat(psiFile.fileType, `is`(XQueryFileType.INSTANCE))
    }

    @Test
    fun testSpaceExistanceTypeBetweenTokens() {
        val parserDefinition = XQueryParserDefinition()
        assertThat(parserDefinition.spaceExistanceTypeBetweenTokens(null, null), `is`(ParserDefinition.SpaceRequirements.MAY))
    }
}
