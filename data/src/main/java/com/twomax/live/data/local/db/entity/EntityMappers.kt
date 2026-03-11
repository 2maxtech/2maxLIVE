package com.twomax.live.data.local.db.entity

import com.twomax.live.core.model.*

// --- Provider ---

fun ProviderEntity.toDomain(): Provider = Provider(
    id = id,
    name = name,
    type = ProviderType.valueOf(type),
    url = url,
    username = username,
    password = password,
    epgUrl = epgUrl,
    isActive = isActive,
    lastSynced = lastSynced
)

fun Provider.toEntity(): ProviderEntity = ProviderEntity(
    id = id,
    name = name,
    type = type.name,
    url = url,
    username = username,
    password = password,
    epgUrl = epgUrl,
    isActive = isActive,
    lastSynced = lastSynced
)

// --- Channel ---

fun ChannelEntity.toDomain(): Channel = Channel(
    id = id,
    providerId = providerId,
    name = name,
    logoUrl = logoUrl,
    groupTitle = groupTitle,
    streamUrl = streamUrl,
    streamType = StreamType.valueOf(streamType),
    epgChannelId = epgChannelId,
    channelNumber = channelNumber,
    isFavorite = isFavorite,
    isHidden = isHidden,
    customOrder = customOrder
)

fun Channel.toEntity(): ChannelEntity = ChannelEntity(
    id = id,
    providerId = providerId,
    name = name,
    logoUrl = logoUrl,
    groupTitle = groupTitle,
    streamUrl = streamUrl,
    streamType = streamType.name,
    epgChannelId = epgChannelId,
    channelNumber = channelNumber,
    isFavorite = isFavorite,
    isHidden = isHidden,
    customOrder = customOrder
)

// --- Movie ---

fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    providerId = providerId,
    vodId = vodId,
    name = name,
    streamUrl = streamUrl,
    coverUrl = coverUrl,
    plot = plot,
    genre = genre,
    rating = rating,
    duration = duration,
    year = year,
    categoryId = categoryId,
    containerExtension = containerExtension,
    isFavorite = isFavorite
)

fun Movie.toEntity(): MovieEntity = MovieEntity(
    id = id,
    providerId = providerId,
    vodId = vodId,
    name = name,
    streamUrl = streamUrl,
    coverUrl = coverUrl,
    plot = plot,
    genre = genre,
    rating = rating,
    duration = duration,
    year = year,
    categoryId = categoryId,
    containerExtension = containerExtension,
    isFavorite = isFavorite
)

// --- Series ---

fun SeriesEntity.toDomain(): Series = Series(
    id = id,
    providerId = providerId,
    seriesId = seriesId,
    name = name,
    coverUrl = coverUrl,
    plot = plot,
    genre = genre,
    rating = rating,
    year = year,
    categoryId = categoryId,
    isFavorite = isFavorite
)

fun Series.toEntity(): SeriesEntity = SeriesEntity(
    id = id,
    providerId = providerId,
    seriesId = seriesId,
    name = name,
    coverUrl = coverUrl,
    plot = plot,
    genre = genre,
    rating = rating,
    year = year,
    categoryId = categoryId,
    isFavorite = isFavorite
)

// --- Episode ---

fun EpisodeEntity.toDomain(): Episode = Episode(
    id = id,
    seriesId = seriesId,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    title = title,
    streamUrl = streamUrl,
    coverUrl = coverUrl,
    plot = plot,
    duration = duration,
    containerExtension = containerExtension
)

fun Episode.toEntity(): EpisodeEntity = EpisodeEntity(
    id = id,
    seriesId = seriesId,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    title = title,
    streamUrl = streamUrl,
    coverUrl = coverUrl,
    plot = plot,
    duration = duration,
    containerExtension = containerExtension
)

// --- EpgProgram ---

fun EpgProgramEntity.toDomain(): EpgProgram = EpgProgram(
    id = id,
    channelEpgId = channelEpgId,
    title = title,
    description = description,
    startTime = startTime,
    endTime = endTime,
    lang = lang
)

fun EpgProgram.toEntity(): EpgProgramEntity = EpgProgramEntity(
    id = id,
    channelEpgId = channelEpgId,
    title = title,
    description = description,
    startTime = startTime,
    endTime = endTime,
    lang = lang
)

// --- Category ---

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    categoryId = categoryId,
    name = name,
    streamType = StreamType.valueOf(streamType),
    providerId = providerId,
    parentId = parentId
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    streamType = streamType.name,
    providerId = providerId,
    parentId = parentId
)
