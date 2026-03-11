package com.twomax.live.data.remote.dto

import com.twomax.live.core.model.Category
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.EpgProgram
import com.twomax.live.core.model.Episode
import com.twomax.live.core.model.Movie
import com.twomax.live.core.model.Series
import com.twomax.live.core.model.StreamType
import com.twomax.live.data.remote.XtreamStreamUrlBuilder

fun XtreamLiveStream.toChannel(
    providerId: Long,
    serverUrl: String,
    username: String,
    password: String
): Channel = Channel(
    providerId = providerId,
    name = name.orEmpty(),
    logoUrl = streamIcon.orEmpty(),
    groupTitle = categoryId.orEmpty(),
    streamUrl = XtreamStreamUrlBuilder.buildLiveStreamUrl(
        serverUrl = serverUrl,
        username = username,
        password = password,
        streamId = streamId ?: 0
    ),
    streamType = StreamType.LIVE,
    epgChannelId = epgChannelId.orEmpty(),
    channelNumber = num ?: 0
)

fun XtreamVodStream.toMovie(
    providerId: Long,
    serverUrl: String,
    username: String,
    password: String
): Movie = Movie(
    providerId = providerId,
    vodId = (streamId ?: 0).toString(),
    name = name.orEmpty(),
    streamUrl = XtreamStreamUrlBuilder.buildVodStreamUrl(
        serverUrl = serverUrl,
        username = username,
        password = password,
        streamId = streamId ?: 0,
        extension = containerExtension ?: "mp4"
    ),
    coverUrl = streamIcon.orEmpty(),
    rating = rating5based ?: 0f,
    categoryId = categoryId.orEmpty(),
    containerExtension = containerExtension.orEmpty()
)

fun XtreamSeriesItem.toSeries(
    providerId: Long
): Series = Series(
    providerId = providerId,
    seriesId = (seriesId ?: 0).toString(),
    name = name.orEmpty(),
    coverUrl = cover.orEmpty(),
    plot = plot.orEmpty(),
    genre = genre.orEmpty(),
    rating = rating5based ?: 0f,
    year = releaseDate.orEmpty(),
    categoryId = categoryId.orEmpty()
)

fun XtreamEpisode.toEpisode(
    seriesId: Long,
    seasonNumber: Int,
    serverUrl: String,
    username: String,
    password: String
): Episode = Episode(
    seriesId = seriesId,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNum ?: 0,
    title = title.orEmpty(),
    streamUrl = XtreamStreamUrlBuilder.buildSeriesStreamUrl(
        serverUrl = serverUrl,
        username = username,
        password = password,
        streamId = id?.toIntOrNull() ?: 0,
        extension = containerExtension ?: "mp4"
    ),
    coverUrl = info?.movieImage.orEmpty(),
    plot = info?.plot.orEmpty(),
    duration = info?.duration.orEmpty(),
    containerExtension = containerExtension.orEmpty()
)

fun XtreamCategory.toCategory(
    providerId: Long,
    streamType: StreamType
): Category = Category(
    categoryId = categoryId.orEmpty(),
    name = categoryName.orEmpty(),
    streamType = streamType,
    providerId = providerId,
    parentId = (parentId ?: 0).toString()
)

fun XtreamEpgListing.toEpgProgram(): EpgProgram = EpgProgram(
    channelEpgId = channelId.orEmpty(),
    title = title.orEmpty(),
    description = description.orEmpty(),
    startTime = startTimestamp?.toLongOrNull() ?: 0L,
    endTime = stopTimestamp?.toLongOrNull() ?: 0L,
    lang = lang.orEmpty()
)
