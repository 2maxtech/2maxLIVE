package com.twomax.live.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class XtreamAuthResponse(
    @Json(name = "user_info") val userInfo: XtreamUserInfo? = null,
    @Json(name = "server_info") val serverInfo: XtreamServerInfo? = null
)

@JsonClass(generateAdapter = true)
data class XtreamUserInfo(
    val username: String? = null,
    val password: String? = null,
    val status: String? = null,
    @Json(name = "exp_date") val expDate: String? = null,
    @Json(name = "is_trial") val isTrial: String? = null,
    @Json(name = "active_cons") val activeCons: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "max_connections") val maxConnections: String? = null,
    @Json(name = "allowed_output_formats") val allowedOutputFormats: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class XtreamServerInfo(
    val url: String? = null,
    val port: String? = null,
    @Json(name = "https_port") val httpsPort: String? = null,
    @Json(name = "server_protocol") val serverProtocol: String? = null,
    @Json(name = "rtmp_port") val rtmpPort: String? = null,
    val timezone: String? = null,
    @Json(name = "timestamp_now") val timestampNow: Long? = null,
    @Json(name = "time_now") val timeNow: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamCategory(
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "parent_id") val parentId: Int? = null
)

@JsonClass(generateAdapter = true)
data class XtreamLiveStream(
    val num: Int? = null,
    val name: String? = null,
    @Json(name = "stream_type") val streamType: String? = null,
    @Json(name = "stream_id") val streamId: Int? = null,
    @Json(name = "stream_icon") val streamIcon: String? = null,
    @Json(name = "epg_channel_id") val epgChannelId: String? = null,
    @Json(name = "added") val added: String? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "custom_sid") val customSid: String? = null,
    @Json(name = "tv_archive") val tvArchive: Int? = null,
    @Json(name = "direct_source") val directSource: String? = null,
    @Json(name = "tv_archive_duration") val tvArchiveDuration: Int? = null
)

@JsonClass(generateAdapter = true)
data class XtreamVodStream(
    val num: Int? = null,
    val name: String? = null,
    @Json(name = "stream_type") val streamType: String? = null,
    @Json(name = "stream_id") val streamId: Int? = null,
    @Json(name = "stream_icon") val streamIcon: String? = null,
    val rating: String? = null,
    @Json(name = "rating_5based") val rating5based: Float? = null,
    val added: String? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "container_extension") val containerExtension: String? = null,
    @Json(name = "custom_sid") val customSid: String? = null,
    @Json(name = "direct_source") val directSource: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamSeriesItem(
    val num: Int? = null,
    val name: String? = null,
    @Json(name = "series_id") val seriesId: Int? = null,
    val cover: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "last_modified") val lastModified: String? = null,
    val rating: String? = null,
    @Json(name = "rating_5based") val rating5based: Float? = null,
    @Json(name = "category_id") val categoryId: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamSeriesInfo(
    val seasons: List<XtreamSeason>? = null,
    val info: XtreamSeriesDetail? = null,
    val episodes: Map<String, List<XtreamEpisode>>? = null
)

@JsonClass(generateAdapter = true)
data class XtreamSeason(
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "episode_count") val episodeCount: Int? = null,
    val id: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    @Json(name = "season_number") val seasonNumber: Int? = null,
    val cover: String? = null,
    @Json(name = "cover_big") val coverBig: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamSeriesDetail(
    val name: String? = null,
    val cover: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    val rating: String? = null,
    @Json(name = "rating_5based") val rating5based: Float? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "backdrop_path") val backdropPath: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class XtreamEpisode(
    val id: String? = null,
    @Json(name = "episode_num") val episodeNum: Int? = null,
    val title: String? = null,
    @Json(name = "container_extension") val containerExtension: String? = null,
    val info: XtreamEpisodeInfo? = null,
    @Json(name = "custom_sid") val customSid: String? = null,
    @Json(name = "added") val added: String? = null,
    val season: Int? = null,
    @Json(name = "direct_source") val directSource: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamEpisodeInfo(
    @Json(name = "movie_image") val movieImage: String? = null,
    val plot: String? = null,
    @Json(name = "duration_secs") val durationSecs: Int? = null,
    val duration: String? = null,
    val rating: Float? = null
)

@JsonClass(generateAdapter = true)
data class XtreamEpgResponse(
    @Json(name = "epg_listings") val epgListings: List<XtreamEpgListing>? = null
)

@JsonClass(generateAdapter = true)
data class XtreamVodInfoResponse(
    val info: XtreamVodDetail? = null,
    @Json(name = "movie_data") val movieData: XtreamVodMovieData? = null
)

@JsonClass(generateAdapter = true)
data class XtreamVodDetail(
    val name: String? = null,
    @Json(name = "cover_big") val coverBig: String? = null,
    @Json(name = "movie_image") val movieImage: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "youtube_trailer") val youtubeTrailer: String? = null,
    val director: String? = null,
    val actors: String? = null,
    val cast: String? = null,
    val description: String? = null,
    val plot: String? = null,
    val genre: String? = null,
    @Json(name = "duration_secs") val durationSecs: Int? = null,
    val duration: String? = null,
    val rating: String? = null,
    @Json(name = "rating_5based") val rating5based: Float? = null,
    @Json(name = "tmdb_id") val tmdbId: String? = null,
    val country: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamVodMovieData(
    @Json(name = "stream_id") val streamId: Int? = null,
    val name: String? = null,
    @Json(name = "container_extension") val containerExtension: String? = null
)

@JsonClass(generateAdapter = true)
data class XtreamEpgListing(
    @Json(name = "epg_id") val epgId: String? = null,
    val title: String? = null,
    val lang: String? = null,
    val start: String? = null,
    val end: String? = null,
    val description: String? = null,
    @Json(name = "channel_id") val channelId: String? = null,
    @Json(name = "start_timestamp") val startTimestamp: String? = null,
    @Json(name = "stop_timestamp") val stopTimestamp: String? = null
)
