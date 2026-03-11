package com.twomax.live.data.parser

import com.twomax.live.core.model.EpgProgram
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XmltvParser @Inject constructor() {

    private val dateFormats = listOf(
        SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyyMMddHHmmss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    )

    fun parse(inputStream: InputStream): Flow<EpgProgram> = flow {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var channelId = ""
        var title = ""
        var description = ""
        var startTime = 0L
        var endTime = 0L
        var lang = ""
        var inProgramme = false
        var currentTag = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    when (parser.name) {
                        "programme" -> {
                            inProgramme = true
                            channelId = parser.getAttributeValue(null, "channel") ?: ""
                            startTime = parseDateTime(parser.getAttributeValue(null, "start") ?: "")
                            endTime = parseDateTime(parser.getAttributeValue(null, "stop") ?: "")
                            title = ""
                            description = ""
                            lang = ""
                        }
                        "title" -> {
                            if (inProgramme) {
                                lang = parser.getAttributeValue(null, "lang") ?: ""
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inProgramme) {
                        when (currentTag) {
                            "title" -> title = parser.text?.trim() ?: ""
                            "desc" -> description = parser.text?.trim() ?: ""
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "programme" && inProgramme) {
                        if (channelId.isNotEmpty() && title.isNotEmpty() && startTime > 0 && endTime > 0) {
                            emit(
                                EpgProgram(
                                    channelEpgId = channelId,
                                    title = title,
                                    description = description,
                                    startTime = startTime,
                                    endTime = endTime,
                                    lang = lang
                                )
                            )
                        }
                        inProgramme = false
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseDateTime(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        for (format in dateFormats) {
            try {
                return format.parse(dateStr)?.time ?: 0L
            } catch (_: Exception) {
                continue
            }
        }
        return 0L
    }
}
