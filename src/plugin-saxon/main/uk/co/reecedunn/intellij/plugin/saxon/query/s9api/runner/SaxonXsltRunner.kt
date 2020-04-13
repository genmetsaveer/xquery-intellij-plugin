/*
 * Copyright (C) 2019-2020 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.saxon.query.s9api.runner

import com.intellij.lang.Language
import com.intellij.openapi.vfs.VirtualFile
import org.w3c.dom.Node
import uk.co.reecedunn.intellij.plugin.core.vfs.decode
import uk.co.reecedunn.intellij.plugin.core.xml.XmlDocument
import uk.co.reecedunn.intellij.plugin.core.xml.XmlElement
import uk.co.reecedunn.intellij.plugin.core.xml.toStreamSource
import uk.co.reecedunn.intellij.plugin.intellij.lang.XPathSubset
import uk.co.reecedunn.intellij.plugin.intellij.resources.PluginApiBundle
import uk.co.reecedunn.intellij.plugin.processor.database.DatabaseModule
import uk.co.reecedunn.intellij.plugin.processor.query.QueryError
import uk.co.reecedunn.intellij.plugin.processor.query.QueryResult
import uk.co.reecedunn.intellij.plugin.processor.query.QueryResults
import uk.co.reecedunn.intellij.plugin.processor.query.RunnableQuery
import uk.co.reecedunn.intellij.plugin.processor.validation.ValidatableQuery
import uk.co.reecedunn.intellij.plugin.saxon.query.s9api.SaxonErrorListener
import uk.co.reecedunn.intellij.plugin.saxon.query.s9api.SaxonQueryResultIterator
import uk.co.reecedunn.intellij.plugin.saxon.query.s9api.binding.*
import uk.co.reecedunn.intellij.plugin.saxon.query.s9api.check
import uk.co.reecedunn.intellij.plugin.xdm.functions.op.op_qname_parse
import uk.co.reecedunn.intellij.plugin.xdm.types.impl.values.XsDuration
import javax.xml.transform.Source
import javax.xml.transform.dom.DOMSource

internal class SaxonXsltRunner(
    val processor: Processor,
    val query: String,
    private val queryFile: VirtualFile
) : RunnableQuery, ValidatableQuery, SaxonRunner {
    private val errorListener = SaxonErrorListener(queryFile, processor.classLoader)

    private val compiler by lazy {
        val ret = processor.newXsltCompiler()
        ret.setErrorListener(errorListener)
        ret
    }

    private val executable by lazy { compiler.compile(query.toStreamSource()) }

    private val transformer by lazy { executable.load() }

    override var rdfOutputFormat: Language? = null

    override var updating: Boolean = false

    override var xpathSubset: XPathSubset = XPathSubset.XPath

    override var server: String = ""

    override var database: String = ""

    override var modulePath: String = ""

    private var context: Source? = null

    override fun bindVariable(name: String, value: Any?, type: String?) = check(queryFile, processor.classLoader) {
        val qname = op_qname_parse(name, SAXON_NAMESPACES).toQName(processor.classLoader)
        transformer.setParameter(qname, XdmValue.newInstance(value, type ?: "xs:string", processor))
    }

    override fun bindContextItem(value: Any?, type: String?): Unit = check(queryFile, processor.classLoader) {
        context = when (value) {
            is DatabaseModule -> value.path.toStreamSource()
            is VirtualFile -> value.decode()?.toStreamSource()
            is XmlDocument -> DOMSource(value.doc)
            is XmlElement -> DOMSource(value.element)
            is Node -> DOMSource(value)
            else -> value?.toString()?.toStreamSource()
        }
    }

    override fun asSequence(): Sequence<QueryResult> = check(queryFile, processor.classLoader, errorListener) {
        if (context == null) {
            // The Saxon processor throws a NPE if source is null.
            val message = PluginApiBundle.message("error.missing-xslt-source")
            return@check sequenceOf(QueryResult.fromItemType(0, message, "fn:error"))
        }
        transformer.setSource(context!!)

        val destination = RawDestination(processor.classLoader)
        transformer.setDestination(destination)

        transformer.transform()
        val result = destination.getXdmValue()

        SaxonQueryResultIterator(result.iterator(), processor).asSequence()
    }

    override fun run(): QueryResults {
        val start = System.nanoTime()
        val results = asSequence().toList()
        val end = System.nanoTime()
        return QueryResults(QueryResults.OK, results, XsDuration.ns(end - start))
    }

    override fun validate(): QueryError? {
        return try {
            check(queryFile, processor.classLoader) { executable } // Compile the query.
            null
        } catch (e: QueryError) {
            e
        }
    }

    override fun close() {
    }
}
