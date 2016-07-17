/*
 * Copyright (C) 2016 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xquery.tests.lexer;

import com.intellij.lexer.Lexer;
import junit.framework.TestCase;
import uk.co.reecedunn.intellij.plugin.xquery.lexer.SyntaxHighlighter;
import uk.co.reecedunn.intellij.plugin.xquery.lexer.SyntaxHighlighterFactory;
import uk.co.reecedunn.intellij.plugin.xquery.lexer.XQueryLexer;
import uk.co.reecedunn.intellij.plugin.xquery.lexer.XQueryTokenType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SyntaxHighlighterTest extends TestCase {
    public void testFactory() {
        SyntaxHighlighterFactory factory = new SyntaxHighlighterFactory();
        com.intellij.openapi.fileTypes.SyntaxHighlighter highlighter = factory.getSyntaxHighlighter(null, null);
        assertThat(highlighter.getClass().getName(), is(SyntaxHighlighter.class.getName()));
    }

    public void testHighlightingLexer() {
        Lexer lexer = new SyntaxHighlighter().getHighlightingLexer();
        assertThat(lexer.getClass().getName(), is(XQueryLexer.class.getName()));
    }

    public void testTokenHighlights() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(null).length, is(0));
    }

    public void testTokenHighlights_BadCharacter() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.BAD_CHARACTER).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.BAD_CHARACTER)[0], is(SyntaxHighlighter.BAD_CHARACTER));
    }

    public void testTokenHighlights_Comment() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.COMMENT_START_TAG).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.COMMENT_START_TAG)[0], is(SyntaxHighlighter.COMMENT));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.COMMENT).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.COMMENT)[0], is(SyntaxHighlighter.COMMENT));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.COMMENT_END_TAG).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.COMMENT_END_TAG)[0], is(SyntaxHighlighter.COMMENT));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.XML_COMMENT_START_TAG).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.XML_COMMENT_START_TAG)[0], is(SyntaxHighlighter.COMMENT));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.XML_COMMENT).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.XML_COMMENT)[0], is(SyntaxHighlighter.COMMENT));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.XML_COMMENT_END_TAG).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.XML_COMMENT_END_TAG)[0], is(SyntaxHighlighter.COMMENT));
    }

    public void testTokenHighlights_Number() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.INTEGER_LITERAL).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.INTEGER_LITERAL)[0], is(SyntaxHighlighter.NUMBER));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.DECIMAL_LITERAL).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.DECIMAL_LITERAL)[0], is(SyntaxHighlighter.NUMBER));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.DOUBLE_LITERAL).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.DOUBLE_LITERAL)[0], is(SyntaxHighlighter.NUMBER));

        // NOTE: This token is for the parser, so that a parser error will be emitted for incomplete double literals.
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PARTIAL_DOUBLE_LITERAL_EXPONENT).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PARTIAL_DOUBLE_LITERAL_EXPONENT)[0], is(SyntaxHighlighter.NUMBER));
    }

    public void testTokenHighlights_String() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_START).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_START)[0], is(SyntaxHighlighter.STRING));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_CONTENTS).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_CONTENTS)[0], is(SyntaxHighlighter.STRING));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_END).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_END)[0], is(SyntaxHighlighter.STRING));
    }

    public void testTokenHighlights_EscapedCharacter() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_ESCAPED_CHARACTER).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STRING_LITERAL_ESCAPED_CHARACTER)[0], is(SyntaxHighlighter.ESCAPED_CHARACTER));
    }

    public void testTokenHighlights_EntityReference() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PREDEFINED_ENTITY_REFERENCE).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PREDEFINED_ENTITY_REFERENCE)[0], is(SyntaxHighlighter.ENTITY_REFERENCE));

        // NOTE: This token is for the parser, so that a parser error will be emitted for invalid entity references.
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.ENTITY_REFERENCE_NOT_IN_STRING).length, is(0));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.CHARACTER_REFERENCE).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.CHARACTER_REFERENCE)[0], is(SyntaxHighlighter.ENTITY_REFERENCE));

        // NOTE: This token is for the parser, so that a parser error will be emitted for invalid entity references.
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PARTIAL_ENTITY_REFERENCE).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PARTIAL_ENTITY_REFERENCE)[0], is(SyntaxHighlighter.STRING));

        // NOTE: This token is for the parser, so that a parser error will be emitted for invalid entity references.
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.EMPTY_ENTITY_REFERENCE).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.EMPTY_ENTITY_REFERENCE)[0], is(SyntaxHighlighter.STRING));
    }

    public void testTokenHighlights_Identifier() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.NCNAME).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.NCNAME)[0], is(SyntaxHighlighter.IDENTIFIER));
    }

    public void testTokenHighlights_Keywords() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.K_ENCODING).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.K_ENCODING)[0], is(SyntaxHighlighter.KEYWORD));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.K_VERSION).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.K_VERSION)[0], is(SyntaxHighlighter.KEYWORD));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.K_XQUERY).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.K_XQUERY)[0], is(SyntaxHighlighter.KEYWORD));
    }

    public void testTokenHighlights_NamespacePrefix() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.QNAME_PREFIX).length, is(1));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.QNAME_PREFIX)[0], is(SyntaxHighlighter.NAMESPACE_PREFIX));
    }

    public void testTokenHighlights_OtherToken() {
        SyntaxHighlighter highlighter = new SyntaxHighlighter();

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.INVALID).length, is(0));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.NOT_EQUAL).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.VARIABLE_INDICATOR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PARENTHESIS_OPEN).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PARENTHESIS_CLOSE).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PRAGMA_BEGIN).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PRAGMA_END).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.STAR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PLUS).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.COMMA).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.MINUS).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.DOT).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.EQUAL).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.BLOCK_OPEN).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.BLOCK_CLOSE).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.SEMICOLON).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.LESS_THAN).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.GREATER_THAN).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.LESS_THAN_OR_EQUAL).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.GREATER_THAN_OR_EQUAL).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.UNION).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.QUESTION).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.AXIS_SEPARATOR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.QNAME_SEPARATOR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.ASSIGN_EQUAL).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.DIRECT_DESCENDANTS_PATH).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.ALL_DESCENDANTS_PATH).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.ATTRIBUTE_SELECTOR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PREDICATE_BEGIN).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PREDICATE_END).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PARENT_SELECTOR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.CLOSE_XML_TAG).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.SELF_CLOSING_XML_TAG).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PROCESSING_INSTRUCTION_BEGIN).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.PROCESSING_INSTRUCTION_END).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.NODE_BEFORE).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.NODE_AFTER).length, is(0));

        assertThat(highlighter.getTokenHighlights(XQueryTokenType.MAP_OPERATOR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.FUNCTION_REF_OPERATOR).length, is(0));
        assertThat(highlighter.getTokenHighlights(XQueryTokenType.ANNOTATION_INDICATOR).length, is(0));
    }
}
