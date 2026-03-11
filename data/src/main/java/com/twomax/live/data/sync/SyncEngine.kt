package com.twomax.live.data.sync

import android.content.Context
import android.util.Log
import com.twomax.live.core.model.*
import com.twomax.live.data.local.db.dao.*
import com.twomax.live.data.local.db.entity.*
import com.twomax.live.data.parser.M3uParser
import com.twomax.live.data.parser.XmltvParser
import com.twomax.live.data.remote.XtreamStreamUrlBuilder
import com.twomax.live.data.remote.api.XtreamApi
import com.twomax.live.data.remote.dto.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val xtreamApi: XtreamApi,
    private val m3uParser: M3uParser,
    private val xmltvParser: XmltvParser,
    private val providerDao: ProviderDao,
    private val channelDao: ChannelDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val categoryDao: CategoryDao,
    private val epgProgramDao: EpgProgramDao,
    private val okHttpClient: OkHttpClient
) {

    sealed class SyncResult {
        data class Success(val channelCount: Int, val movieCount: Int, val seriesCount: Int) : SyncResult()
        data class Error(val message: String) : SyncResult()
    }

    suspend fun syncProvider(provider: Provider): SyncResult = withContext(Dispatchers.IO) {
        try {
            when (provider.type) {
                ProviderType.M3U_URL -> syncM3uUrl(provider)
                ProviderType.M3U_FILE -> syncM3uFile(provider)
                ProviderType.XTREAM -> syncXtream(provider)
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during sync")
        }
    }

    private suspend fun syncM3uUrl(provider: Provider): SyncResult {
        val request = Request.Builder()
            .url(provider.url)
            .header("User-Agent", "Mozilla/5.0 (compatible; IPTV)")
            .build()
        val response = okHttpClient.newCall(request).execute()
        val body = response.body ?: return SyncResult.Error("No response body (HTTP ${response.code})")
        return syncM3uStream(body.byteStream(), provider)
    }

    private suspend fun syncM3uFile(provider: Provider): SyncResult {
        val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(provider.url))
            ?: return SyncResult.Error("Cannot open file")
        return syncM3uStream(inputStream, provider)
    }

    private suspend fun syncM3uStream(inputStream: InputStream, provider: Provider): SyncResult {
        // Clear existing data for this provider
        channelDao.deleteByProvider(provider.id)
        movieDao.deleteByProvider(provider.id)
        seriesDao.deleteByProvider(provider.id)
        categoryDao.deleteByProvider(provider.id)

        val channels = mutableListOf<Channel>()
        val movies = mutableListOf<Channel>()
        val series = mutableListOf<Channel>()
        val categories = mutableSetOf<String>()

        m3uParser.parse(inputStream, provider.id).collect { channel ->
            when (channel.streamType) {
                StreamType.LIVE -> channels.add(channel)
                StreamType.MOVIE -> movies.add(channel)
                StreamType.SERIES -> series.add(channel)
            }
            if (channel.groupTitle.isNotEmpty()) {
                categories.add(channel.groupTitle)
            }
        }

        // Persist channels (all types stored as channels for M3U)
        val allChannels = channels + movies + series
        Log.d("SyncEngine", "M3U sync: live=${channels.size}, movies=${movies.size}, series=${series.size}, total=${allChannels.size}")
        if (allChannels.isEmpty()) {
            return SyncResult.Error("No channels found in M3U. Check that the URL returns a valid M3U playlist.")
        }
        channelDao.insertAll(allChannels.map { it.toEntity() })

        // Persist categories
        val categoryEntities = categories.mapIndexed { index, name ->
            val type = when {
                allChannels.any { it.groupTitle == name && it.streamType == StreamType.MOVIE } -> StreamType.MOVIE
                allChannels.any { it.groupTitle == name && it.streamType == StreamType.SERIES } -> StreamType.SERIES
                else -> StreamType.LIVE
            }
            CategoryEntity(
                categoryId = index.toString(),
                name = name,
                streamType = type.name,
                providerId = provider.id
            )
        }
        categoryDao.insertAll(categoryEntities)

        // Update provider last synced
        providerDao.update(provider.toEntity().copy(lastSynced = System.currentTimeMillis()))

        return SyncResult.Success(channels.size, movies.size, series.size)
    }

    private suspend fun syncXtream(provider: Provider): SyncResult {
        val apiUrl = XtreamStreamUrlBuilder.buildApiUrl(provider.url)

        // Authenticate
        val auth = xtreamApi.authenticate(apiUrl, provider.username, provider.password)
        // Some providers return null status or other strings - just check we got a response
        if (auth.userInfo == null && auth.serverInfo == null) {
            return SyncResult.Error("Authentication failed - check server URL, username and password")
        }

        // Clear existing data
        channelDao.deleteByProvider(provider.id)
        movieDao.deleteByProvider(provider.id)
        seriesDao.deleteByProvider(provider.id)
        categoryDao.deleteByProvider(provider.id)

        // Fetch categories and build id→name maps
        val liveCategories = xtreamApi.getLiveCategories(apiUrl, provider.username, provider.password, "get_live_categories")
        val vodCategories = xtreamApi.getVodCategories(apiUrl, provider.username, provider.password, "get_vod_categories")
        val seriesCategories = xtreamApi.getSeriesCategories(apiUrl, provider.username, provider.password, "get_series_categories")

        val liveCatMap = liveCategories.associate { (it.categoryId ?: "") to (it.categoryName ?: "") }
        val vodCatMap = vodCategories.associate { (it.categoryId ?: "") to (it.categoryName ?: "") }
        val seriesCatMap = seriesCategories.associate { (it.categoryId ?: "") to (it.categoryName ?: "") }

        categoryDao.insertAll(
            liveCategories.map { it.toCategory(provider.id, StreamType.LIVE).toEntity() } +
            vodCategories.map { it.toCategory(provider.id, StreamType.MOVIE).toEntity() } +
            seriesCategories.map { it.toCategory(provider.id, StreamType.SERIES).toEntity() }
        )

        // Fetch live streams → store in channels table with category name as groupTitle
        val liveStreams = xtreamApi.getLiveStreams(apiUrl, provider.username, provider.password, "get_live_streams")
            .distinctBy { it.streamId }
        val channelEntities = liveStreams.map { stream ->
            val catName = liveCatMap[stream.categoryId.orEmpty()].orEmpty().ifEmpty { "All Channels" }
            stream.toChannel(provider.id, provider.url, provider.username, provider.password)
                .copy(groupTitle = catName)
                .toEntity()
        }
        channelDao.insertAll(channelEntities)

        // Fetch VOD streams → store in BOTH movieDao AND channelDao (for unified browsing)
        val vodStreams = xtreamApi.getVodStreams(apiUrl, provider.username, provider.password, "get_vod_streams")
            .distinctBy { it.streamId }
        movieDao.insertAll(vodStreams.map {
            it.toMovie(provider.id, provider.url, provider.username, provider.password).toEntity()
        })
        val vodChannelEntities = vodStreams.map { stream ->
            val catName = vodCatMap[stream.categoryId.orEmpty()].orEmpty().ifEmpty { "All Movies" }
            ChannelEntity(
                providerId = provider.id,
                name = stream.name.orEmpty(),
                logoUrl = stream.streamIcon.orEmpty(),
                groupTitle = catName,
                streamUrl = XtreamStreamUrlBuilder.buildVodStreamUrl(
                    serverUrl = provider.url,
                    username = provider.username,
                    password = provider.password,
                    streamId = stream.streamId ?: 0,
                    extension = stream.containerExtension ?: "mp4"
                ),
                streamType = StreamType.MOVIE.name,
                channelNumber = 0
            )
        }
        channelDao.insertAll(vodChannelEntities)

        // Fetch series → store in BOTH seriesDao AND channelDao (for unified browsing)
        val seriesList = xtreamApi.getSeries(apiUrl, provider.username, provider.password, "get_series")
            .distinctBy { it.seriesId }
        seriesDao.insertAll(seriesList.map { it.toSeries(provider.id).toEntity() })
        val seriesChannelEntities = seriesList.map { item ->
            val catName = seriesCatMap[item.categoryId.orEmpty()].orEmpty().ifEmpty { "All Series" }
            ChannelEntity(
                providerId = provider.id,
                name = item.name.orEmpty(),
                logoUrl = item.cover.orEmpty(),
                groupTitle = catName,
                streamUrl = "",
                streamType = StreamType.SERIES.name,
                channelNumber = 0
            )
        }
        channelDao.insertAll(seriesChannelEntities)

        // Update provider last synced
        val xmltvUrl = XtreamStreamUrlBuilder.buildXmltvUrl(provider.url, provider.username, provider.password)
        providerDao.update(provider.toEntity().copy(lastSynced = System.currentTimeMillis(), epgUrl = xmltvUrl))

        Log.d("SyncEngine", "Xtream sync: live=${channelEntities.size}, vod=${vodChannelEntities.size}, series=${seriesChannelEntities.size}")

        return SyncResult.Success(channelEntities.size, vodChannelEntities.size, seriesChannelEntities.size)
    }

    private suspend fun syncXtreamEpg(xmltvUrl: String, knownEpgIds: List<String>) {
        Log.d("SyncEngine", "Fetching EPG from $xmltvUrl")
        val request = Request.Builder()
            .url(xmltvUrl)
            .header("User-Agent", "Mozilla/5.0 (compatible; IPTV)")
            .build()
        val response = okHttpClient.newCall(request).execute()
        val body = response.body ?: run {
            Log.w("SyncEngine", "EPG response body is null (HTTP ${response.code})")
            return
        }

        // Clear old EPG data for these channels before re-inserting
        if (knownEpgIds.isNotEmpty()) {
            epgProgramDao.deleteByChannelIds(knownEpgIds)
        } else {
            epgProgramDao.deleteAll()
        }

        var batch = mutableListOf<EpgProgramEntity>()
        var total = 0
        body.byteStream().use { stream ->
            xmltvParser.parse(stream).collect { program ->
                batch.add(program.toEntity())
                if (batch.size >= 500) {
                    epgProgramDao.insertAll(batch)
                    total += batch.size
                    batch = mutableListOf()
                }
            }
        }
        if (batch.isNotEmpty()) {
            epgProgramDao.insertAll(batch)
            total += batch.size
        }
        Log.d("SyncEngine", "EPG sync complete: $total programs stored")
    }
}
