package com.twomax.live.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.twomax.live.data.local.db.dao.PublicEpgSourceDao
import com.twomax.live.data.local.db.entity.toEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SeedEpgSourcesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val publicEpgSourceDao: PublicEpgSourceDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (publicEpgSourceDao.count() == 0) {
            publicEpgSourceDao.insertAll(CuratedEpgSources.all.map { it.toEntity() })
        }
        // Ensure curated sources are enabled (handles fresh + existing installs)
        publicEpgSourceDao.enableAllCurated()
        return Result.success()
    }
}
