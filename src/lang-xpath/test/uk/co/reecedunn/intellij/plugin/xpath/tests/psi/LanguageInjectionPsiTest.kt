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
package uk.co.reecedunn.intellij.plugin.xpath.tests.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.co.reecedunn.intellij.plugin.core.tests.assertion.assertThat
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.*
import uk.co.reecedunn.intellij.plugin.xpath.tests.parser.ParserTestCase

// NOTE: This class is private so the JUnit 4 test runner does not run the tests contained in it.
@DisplayName("XQuery IntelliJ Plugin - Language Injection Host - XPath")
private class LanguageInjectionPsiTest : ParserTestCase() {
    @Nested
    @DisplayName("relevant text range")
    internal inner class RelevantTextRange {
        @Nested
        @DisplayName("XPath 3.1 EBNF (116) StringLiteral")
        internal inner class StringLiteral {
            @Test
            @DisplayName("string literal content")
            fun stringLiteral() {
                val host = parse<XPathStringLiteral>("\"Lorem ipsum.\"")[0] as PsiLanguageInjectionHost
                assertThat(host.createLiteralTextEscaper().relevantTextRange, `is`(TextRange(1, 13)))
            }

            @Test
            @DisplayName("unclosed string literal content")
            fun unclosedStringLiteral() {
                val host = parse<XPathStringLiteral>("\"Lorem ipsum.")[0] as PsiLanguageInjectionHost
                assertThat(host.createLiteralTextEscaper().relevantTextRange, `is`(TextRange(1, 13)))
            }
        }
    }

    @Nested
    @DisplayName("decode")
    internal inner class Decode {
        @Nested
        @DisplayName("XPath 3.1 EBNF (116) StringLiteral")
        internal inner class StringLiteral {
            @Test
            @DisplayName("string literal content")
            fun stringLiteral() {
                val host = parse<XPathStringLiteral>("\"test\"")[0] as PsiLanguageInjectionHost
                val escaper = host.createLiteralTextEscaper()

                val range = escaper.relevantTextRange
                val builder = StringBuilder()
                assertThat(escaper.decode(range, builder), `is`(true))

                assertThat(builder.toString(), `is`("test"))
            }

            @Test
            @DisplayName("EscapeApos tokens")
            fun escapeApos() {
                val host = parse<XPathStringLiteral>("'''\"\"'")[0] as PsiLanguageInjectionHost
                val escaper = host.createLiteralTextEscaper()

                val range = escaper.relevantTextRange
                val builder = StringBuilder()
                assertThat(escaper.decode(range, builder), `is`(true))

                assertThat(builder.toString(), `is`("'\"\""))
            }

            @Test
            @DisplayName("EscapeQuot tokens")
            fun escapeQuot() {
                val host = parse<XPathStringLiteral>("\"''\"\"\"")[0] as PsiLanguageInjectionHost
                val escaper = host.createLiteralTextEscaper()

                val range = escaper.relevantTextRange
                val builder = StringBuilder()
                assertThat(escaper.decode(range, builder), `is`(true))

                assertThat(builder.toString(), `is`("''\""))
            }
        }
    }
}