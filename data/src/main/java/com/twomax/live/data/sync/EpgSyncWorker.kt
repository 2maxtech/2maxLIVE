package com.twomax.live.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.twomax.live.data.local.db.dao.EpgProgramDao
import com.twomax.live.data.local.db.dao.ProviderDao
import com.twomax.live.data.local.db.dao.PublicEpgSourceDao
import com.twomax.live.data.local.db.entity.EpgProgramEntity
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.parser.XmltvParser
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Named

@HiltWorker
class EpgSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val providerDao: ProviderDao,
    private val epgProgramDao: EpgProgramDao,
    private val xmltvParser: XmltvParser,
    private val okHttpClient: OkHttpClient,
    @Named("epg") private val epgOkHttpClient: OkHttpClient,
    private val publicEpgSourceDao: PublicEpgSourceDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val providers = providerDao.getAll().firstOrNull() ?: return@withContext Result.success()

            // Clean old programs (older than 24h)
            epgProgramDao.deleteOlderThan(System.currentTimeMillis() - 86400000)

            providers.forEach { providerEntity ->
                val provider = providerEntity.toDomain()
                if (provider.epgUrl.isNotEmpty()) {
                    try {
                        val request = Request.Builder().url(provider.epgUrl).build()
                        val response = okHttpClient.newCall(request).execute()
                        val body = response.body ?: return@forEach

                        val programs = mutableListOf<EpgProgramEntity>()
                        xmltvParser.parse(body.byteStream()).collect { program ->
                            programs.add(
                                EpgProgramEntity(
                                    channelEpgId = program.channelEpgId,
                                    title = program.title,
                                    description = program.description,
                                    startTime = program.startTime,
                                    endTime = program.endTime,
                                    lang = program.lang
                                )
                            )
                            // Batch insert every 500 programs
                            if (programs.size >= 500) {
                                epgProgramDao.insertAll(programs.toList())
                                programs.clear()
                            }
                        }
                        // Insert remaining
                        if (programs.isNotEmpty()) {
                            epgProgramDao.insertAll(programs)
                        }
                    } catch (e: Exception) {
                        // Continue with other providers
                    }
                }
            }

            // Fetch enabled public EPG sources
            val publicSources = publicEpgSourceDao.getEnabled()
            publicSources.forEach { source ->
                try {
                    val response = epgOkHttpClient.newCall(
                        Request.Builder().url(source.url).build()
                    ).execute()
                    val body = response.body ?: return@forEach
                    val batch = mutableListOf<EpgProgramEntity>()
                    xmltvParser.parse(body.byteStream()).collect { program ->
                        batch.add(
                            EpgProgramEntity(
                                channelEpgId = program.channelEpgId,
                                title = program.title,
                                description = program.description,
                                startTime = program.startTime,
                                endTime = program.endTime,
                                lang = program.lang
                            )
                        )
                        if (batch.size >= 500) {
                            epgProgramDao.insertAll(batch.toList())
                            batch.clear()
                        }
                    }
                    if (batch.isNotEmpty()) epgProgramDao.insertAll(batch)
                } catch (e: Exception) {
                    Log.e("EpgSyncWorker", "Failed to fetch public source ${source.label}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
