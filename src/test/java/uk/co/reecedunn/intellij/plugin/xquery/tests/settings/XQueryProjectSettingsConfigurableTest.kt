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
package uk.co.reecedunn.intellij.plugin.xquery.tests.settings

import com.intellij.ide.ui.UISettings
import com.intellij.lang.LanguageASTFactory
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.co.reecedunn.intellij.plugin.core.tests.assertion.assertThat
import uk.co.reecedunn.intellij.plugin.core.tests.parser.ParsingTestCase
import uk.co.reecedunn.intellij.plugin.xquery.ast.xquery.XQueryModule
import uk.co.reecedunn.intellij.plugin.xquery.parser.XQueryASTFactory
import uk.co.reecedunn.intellij.plugin.xquery.parser.XQueryParserDefinition
import uk.co.reecedunn.intellij.plugin.xquery.settings.XQueryProjectSettings
import uk.co.reecedunn.intellij.plugin.xquery.settings.XQueryProjectSettingsConfigurable

// NOTE: This class is private so the JUnit 4 test runner does not run the tests contained in it.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
private class XQueryProjectSettingsConfigurableTest : ParsingTestCase<XQueryModule>("xqy", XQueryParserDefinition()) {
    @BeforeAll
    override fun setUp() {
        super.setUp()

        registerApplicationService(XQueryProjectSettings::class.java, XQueryProjectSettings())
        registerApplicationService(UISettings::class.java, UISettings())

        addExplicitExtension(LanguageASTFactory.INSTANCE, language!!, XQueryASTFactory())
    }

    @AfterAll
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun testDisplayName() {
        val configurable = XQueryProjectSettingsConfigurable(myProject)
        assertThat(configurable.displayName, `is`("XQuery"))
    }

    @Test
    fun testHelpTopic() {
        val configurable = XQueryProjectSettingsConfigurable(myProject)
        assertThat(configurable.helpTopic, `is`(nullValue()))
    }
}
