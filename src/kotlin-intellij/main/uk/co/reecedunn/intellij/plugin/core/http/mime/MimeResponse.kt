/*
 * Copyright (C) 2017-2019 Reece H. Dunn
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
package uk.co.reecedunn.intellij.plugin.core.http.mime

import org.apache.http.Header
import org.apache.http.message.BasicHeader
import java.nio.charset.Charset

import java.util.ArrayList

private fun String.decode(charset: Charset): String {
    return String(toByteArray(Charsets.ISO_8859_1), charset)
}

class MimeResponse(headers: Array<Header>, body: String, private val defaultEncoding: Charset) {
    private val message: Message = Message(headers, body)
    val parts: Array<Message>

    init {
        val messages = ArrayList<Message>()
        val contentType = message.getHeaders("Content-Type")
            .filter { contentType -> contentType.startsWith("multipart/mixed; boundary=") }
            .firstOrNull()
        if (contentType != null) {
            body.split(("\r\n--" + contentType.split("boundary=".toRegex())[1]).toRegex())
                .asSequence()
                .filter { it.isNotEmpty() && it != "--\r\n" }
                .map { it.split("\r\n\r\n".toRegex(), 2) }
                .mapTo(messages) { Message(parseHeaders(it[0]), it[1].decode(defaultEncoding)) }
        } else {
            messages.add(message)
        }
        this.parts = messages.toTypedArray()
    }

    fun getHeader(header: String): String? = message.getHeader(header)

    private fun parseHeaders(content: String): Array<Header> {
        val headers = ArrayList<Header>()
        content.split("\r\n".toRegex()).forEach { header ->
            if (header.isEmpty()) {
                return@forEach
            }

            val nameValue = header.split(":\\s+".toRegex())
            headers.add(BasicHeader(nameValue[0], nameValue[1]))
        }
        return headers.toTypedArray()
    }
}
