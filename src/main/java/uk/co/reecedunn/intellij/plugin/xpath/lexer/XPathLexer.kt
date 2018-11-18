/*
 * Copyright (C) 2018 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xpath.lexer

import uk.co.reecedunn.intellij.plugin.core.lexer.CharacterClass
import uk.co.reecedunn.intellij.plugin.core.lexer.CodePointRange
import uk.co.reecedunn.intellij.plugin.core.lexer.LexerImpl
import uk.co.reecedunn.intellij.plugin.core.lexer.STATE_DEFAULT

// region State Constants

const val STATE_STRING_LITERAL_QUOTE = 1
const val STATE_STRING_LITERAL_APOSTROPHE = 2
const val STATE_DOUBLE_EXPONENT = 3
const val STATE_XQUERY_COMMENT = 4
const val STATE_UNEXPECTED_END_OF_BLOCK = 6
const val STATE_BRACED_URI_LITERAL = 26

// endregion

private val KEYWORDS = mapOf(
    "for" to XPathTokenType.K_FOR, // XPath 2.0
    "in" to XPathTokenType.K_IN, // XPath 2.0
    "return" to XPathTokenType.K_RETURN // XPath 2.0
)

open class XPathLexer : LexerImpl(STATE_DEFAULT) {
    // region States

    protected open fun ncnameToKeyword(name: CharSequence): IKeywordOrNCNameType? {
        return KEYWORDS[name]
    }

    private fun stateDefault() {
        var c = mTokenRange.codePoint
        var cc = CharacterClass.getCharClass(c)
        when (cc) {
            CharacterClass.COLON -> {
                mTokenRange.match()
                c = mTokenRange.codePoint
                mType = if (c == ')'.toInt()) {
                    mTokenRange.match()
                    XPathTokenType.COMMENT_END_TAG
                } else {
                    XPathTokenType.QNAME_SEPARATOR
                }
            }
            CharacterClass.COMMA -> {
                mTokenRange.match()
                mType = XPathTokenType.COMMA
            }
            CharacterClass.PARENTHESIS_OPEN -> {
                mTokenRange.match()
                c = mTokenRange.codePoint
                if (c == ':'.toInt()) {
                    mTokenRange.match()
                    mType = XPathTokenType.COMMENT_START_TAG
                    pushState(STATE_XQUERY_COMMENT)
                }
            }
            CharacterClass.DIGIT -> {
                mTokenRange.match()
                while (CharacterClass.getCharClass(mTokenRange.codePoint) == CharacterClass.DIGIT)
                    mTokenRange.match()
                mType = if (CharacterClass.getCharClass(mTokenRange.codePoint) == CharacterClass.DOT) {
                    mTokenRange.match()
                    while (CharacterClass.getCharClass(mTokenRange.codePoint) == CharacterClass.DIGIT)
                        mTokenRange.match()
                    XPathTokenType.DECIMAL_LITERAL
                } else {
                    XPathTokenType.INTEGER_LITERAL
                }
                c = mTokenRange.codePoint
                if (c == 'e'.toInt() || c == 'E'.toInt()) {
                    mTokenRange.save()
                    mTokenRange.match()
                    c = mTokenRange.codePoint
                    if (c == '+'.toInt() || c == '-'.toInt()) {
                        mTokenRange.match()
                        c = mTokenRange.codePoint
                    }
                    if (CharacterClass.getCharClass(c) == CharacterClass.DIGIT) {
                        mTokenRange.match()
                        while (CharacterClass.getCharClass(mTokenRange.codePoint) == CharacterClass.DIGIT)
                            mTokenRange.match()
                        mType = XPathTokenType.DOUBLE_LITERAL
                    } else {
                        pushState(STATE_DOUBLE_EXPONENT)
                        mTokenRange.restore()
                    }
                }
            }
            CharacterClass.DOLLAR -> {
                mTokenRange.match()
                mType = XPathTokenType.VARIABLE_INDICATOR
            }
            CharacterClass.DOT -> {
                mTokenRange.match()
                while (CharacterClass.getCharClass(mTokenRange.codePoint) == CharacterClass.DIGIT)
                    mTokenRange.match()
                mType = XPathTokenType.DECIMAL_LITERAL
                c = mTokenRange.codePoint
                if (c == 'e'.toInt() || c == 'E'.toInt()) {
                    mTokenRange.save()
                    mTokenRange.match()
                    c = mTokenRange.codePoint
                    if (c == '+'.toInt() || c == '-'.toInt()) {
                        mTokenRange.match()
                        c = mTokenRange.codePoint
                    }
                    if (CharacterClass.getCharClass(c) == CharacterClass.DIGIT) {
                        mTokenRange.match()
                        while (CharacterClass.getCharClass(mTokenRange.codePoint) == CharacterClass.DIGIT)
                            mTokenRange.match()
                        mType = XPathTokenType.DOUBLE_LITERAL
                    } else {
                        pushState(STATE_DOUBLE_EXPONENT)
                        mTokenRange.restore()
                    }
                }
            }
            CharacterClass.NAME_START_CHAR -> {
                mTokenRange.match()
                cc = CharacterClass.getCharClass(mTokenRange.codePoint)
                if (c == 'Q'.toInt() && cc == CharacterClass.CURLY_BRACE_OPEN) {
                    mTokenRange.match()
                    mType = XPathTokenType.BRACED_URI_LITERAL_START
                    pushState(STATE_BRACED_URI_LITERAL)
                } else {
                    while (
                        cc == CharacterClass.NAME_START_CHAR ||
                        cc == CharacterClass.DIGIT ||
                        cc == CharacterClass.DOT ||
                        cc == CharacterClass.HYPHEN_MINUS ||
                        cc == CharacterClass.NAME_CHAR
                    ) {
                        mTokenRange.match()
                        cc = CharacterClass.getCharClass(mTokenRange.codePoint)
                    }
                    mType = ncnameToKeyword(tokenText) ?: XPathTokenType.NCNAME
                }
            }
            CharacterClass.QUOTE, CharacterClass.APOSTROPHE -> {
                mTokenRange.match()
                mType = XPathTokenType.STRING_LITERAL_START
                pushState(if (cc == CharacterClass.QUOTE) STATE_STRING_LITERAL_QUOTE else STATE_STRING_LITERAL_APOSTROPHE)
            }
            CharacterClass.WHITESPACE -> {
                mTokenRange.match()
                while (CharacterClass.getCharClass(mTokenRange.codePoint) == CharacterClass.WHITESPACE)
                    mTokenRange.match()
                mType = XPathTokenType.WHITE_SPACE
            }
            CharacterClass.END_OF_BUFFER -> mType = null
            else -> {
                mTokenRange.match()
                mType = XPathTokenType.BAD_CHARACTER
            }
        }
    }

    protected open fun stateStringLiteral(type: Char) {
        var c = mTokenRange.codePoint
        if (c == type.toInt()) {
            mTokenRange.match()
            if (mTokenRange.codePoint == type.toInt() && type != '}') {
                mTokenRange.match()
                mType = XPathTokenType.ESCAPED_CHARACTER
            } else {
                mType = if (type == '}') XPathTokenType.BRACED_URI_LITERAL_END else XPathTokenType.STRING_LITERAL_END
                popState()
            }
        } else if (c == '{'.toInt() && type == '}') {
            mTokenRange.match()
            mType = XPathTokenType.BAD_CHARACTER
        } else if (c == CodePointRange.END_OF_BUFFER) {
            mType = null
        } else {
            while (c != type.toInt() && c != CodePointRange.END_OF_BUFFER && c != '&'.toInt() && !(type == '}' && c == '{'.toInt())) {
                mTokenRange.match()
                c = mTokenRange.codePoint
            }
            mType = XPathTokenType.STRING_LITERAL_CONTENTS
        }
    }

    private fun stateDoubleExponent() {
        mTokenRange.match()
        val c = mTokenRange.codePoint
        if (c == '+'.toInt() || c == '-'.toInt()) {
            mTokenRange.match()
        }
        mType = XPathTokenType.PARTIAL_DOUBLE_LITERAL_EXPONENT
        popState()
    }

    private fun stateXQueryComment() {
        var c = mTokenRange.codePoint
        if (c == CodePointRange.END_OF_BUFFER) {
            mType = null
            return
        } else if (c == ':'.toInt()) {
            mTokenRange.save()
            mTokenRange.match()
            if (mTokenRange.codePoint == ')'.toInt()) {
                mTokenRange.match()
                mType = XPathTokenType.COMMENT_END_TAG
                popState()
                return
            } else {
                mTokenRange.restore()
            }
        }

        var depth = 1
        while (true) {
            if (c == CodePointRange.END_OF_BUFFER) {
                mTokenRange.match()
                mType = XPathTokenType.COMMENT
                popState()
                pushState(STATE_UNEXPECTED_END_OF_BLOCK)
                return
            } else if (c == '('.toInt()) {
                mTokenRange.match()
                if (mTokenRange.codePoint == ':'.toInt()) {
                    mTokenRange.match()
                    ++depth
                }
            } else if (c == ':'.toInt()) {
                mTokenRange.save()
                mTokenRange.match()
                if (mTokenRange.codePoint == ')'.toInt()) {
                    mTokenRange.match()
                    if (--depth == 0) {
                        mTokenRange.restore()
                        mType = XPathTokenType.COMMENT
                        return
                    }
                }
            } else {
                mTokenRange.match()
            }
            c = mTokenRange.codePoint
        }
    }

    private fun stateUnexpectedEndOfBlock() {
        mType = XPathTokenType.UNEXPECTED_END_OF_BLOCK
        popState()
    }

    // endregion
    // region Lexer

    override fun advance() {
        when (nextState()) {
            STATE_DEFAULT -> stateDefault()
            STATE_STRING_LITERAL_QUOTE -> stateStringLiteral('"')
            STATE_STRING_LITERAL_APOSTROPHE -> stateStringLiteral('\'')
            STATE_DOUBLE_EXPONENT -> stateDoubleExponent()
            STATE_XQUERY_COMMENT -> stateXQueryComment()
            STATE_UNEXPECTED_END_OF_BLOCK -> stateUnexpectedEndOfBlock()
            STATE_BRACED_URI_LITERAL -> stateStringLiteral('}')
            else -> throw AssertionError("Invalid state: $state")
        }
    }

    // endregion
}
