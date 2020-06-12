/*
 * Copyright (C) 2016-2020 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.xpath.tests.annotation

import com.intellij.lang.LanguageASTFactory
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import uk.co.reecedunn.intellij.plugin.core.tests.parser.AnnotatorTestCase
import uk.co.reecedunn.intellij.plugin.xpath.ast.xpath.XPath
import uk.co.reecedunn.intellij.plugin.xpath.parser.XPathASTFactory
import uk.co.reecedunn.intellij.plugin.xpath.parser.XPathParserDefinition

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AnnotatorTestCase : AnnotatorTestCase<XPath>("xqy", XPathParserDefinition()) {
    @BeforeAll
    override fun setUp() {
        super.setUp()
        addExplicitExtension(
            LanguageASTFactory.INSTANCE, uk.co.reecedunn.intellij.plugin.xpath.intellij.lang.XPath, XPathASTFactory()
        )
    }

    @AfterAll
    override fun tearDown() {
        super.tearDown()
    }
}
