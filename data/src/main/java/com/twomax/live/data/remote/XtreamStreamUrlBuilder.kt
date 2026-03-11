package com.twomax.live.data.remote

object XtreamStreamUrlBuilder {
    fun buildLiveStreamUrl(
        serverUrl: String,
        username: String,
        password: String,
        streamId: Int,
        extension: String = "ts"
    ): String {
        val base = serverUrl.trimEnd('/')
        return "$base/live/$username/$password/$streamId.$extension"
    }

    fun buildVodStreamUrl(
        serverUrl: String,
        username: String,
        password: String,
        streamId: Int,
        extension: String = "mp4"
    ): String {
        val base = serverUrl.trimEnd('/')
        return "$base/movie/$username/$password/$streamId.$extension"
    }

    fun buildSeriesStreamUrl(
        serverUrl: String,
        username: String,
        password: String,
        streamId: Int,
        extension: String = "mp4"
    ): String {
        val base = serverUrl.trimEnd('/')
        return "$base/series/$username/$password/$streamId.$extension"
    }

    fun buildApiUrl(serverUrl: String): String {
        val base = serverUrl.trimEnd('/')
        return "$base/player_api.php"
    }

    fun buildXmltvUrl(serverUrl: String, username: String, password: String): String {
        val base = serverUrl.trimEnd('/')
        return "$base/xmltv.php?username=$username&password=$password"
    }
}
