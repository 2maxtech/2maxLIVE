package com.twomax.live.data.parser

import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.StreamType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uParser @Inject constructor() {

    fun parse(inputStream: InputStream, providerId: Long): Flow<Channel> = flow {
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        var currentExtInf: String? = null
        var channelNumber = 0

        while (reader.readLine().also { line = it } != null) {
            val trimmed = line?.trim() ?: continue

            when {
                trimmed.startsWith("#EXTM3U") -> continue
                trimmed.startsWith("#EXTINF") -> {
                    currentExtInf = trimmed
                }
                trimmed.isNotEmpty() && !trimmed.startsWith("#") && currentExtInf != null -> {
                    val channel = parseChannel(currentExtInf, trimmed, providerId, ++channelNumber)
                    if (channel != null) {
                        emit(channel)
                    }
                    currentExtInf = null
                }
            }
        }
        reader.close()
    }

    private fun parseChannel(extInf: String, url: String, providerId: Long, number: Int): Channel? {
        if (url.isBlank()) return null

        val tvgId = extractAttribute(extInf, "tvg-id")
        val tvgName = extractAttribute(extInf, "tvg-name")
        val tvgLogo = extractAttribute(extInf, "tvg-logo")
        val groupTitle = extractAttribute(extInf, "group-title")

        // Display name is after the last comma in #EXTINF line
        val displayName = extInf.substringAfterLast(",", "").trim()
        val name = displayName.ifEmpty { tvgName }

        if (name.isBlank()) return null

        val streamType = determineStreamType(url, groupTitle)
        val effectiveGroup = groupTitle.ifEmpty {
            when (streamType) {
                StreamType.MOVIE -> "All Movies"
                StreamType.SERIES -> "All Series"
                StreamType.LIVE -> "All Channels"
            }
        }

        return Channel(
            providerId = providerId,
            name = name,
            logoUrl = tvgLogo,
            groupTitle = effectiveGroup,
            streamUrl = url,
            streamType = streamType,
            epgChannelId = tvgId,
            channelNumber = number
        )
    }

    private fun extractAttribute(extInf: String, attribute: String): String {
        val regex = Regex("""$attribute="([^"]*?)"""")
        return regex.find(extInf)?.groupValues?.getOrNull(1) ?: ""
    }

    private fun determineStreamType(url: String, groupTitle: String): StreamType {
        val urlLower = url.lowercase()
        val groupLower = groupTitle.lowercase()

        return when {
            urlLower.contains("/movie/") || urlLower.contains("/vod/") -> StreamType.MOVIE
            urlLower.contains("/series/") -> StreamType.SERIES
            groupLower.contains("movie") || groupLower.contains("film") || groupLower.contains("vod") -> StreamType.MOVIE
            groupLower.contains("series") || groupLower.contains("show") -> StreamType.SERIES
            else -> StreamType.LIVE
        }
    }
}
