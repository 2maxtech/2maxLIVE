package com.twomax.live.data.repository

import com.twomax.live.core.model.PublicEpgSource
import com.twomax.live.core.repository.PublicEpgSourceRepository
import com.twomax.live.data.local.db.dao.PublicEpgSourceDao
import com.twomax.live.data.local.db.entity.PublicEpgSourceEntity
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.local.db.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PublicEpgSourceRepositoryImpl @Inject constructor(
    private val dao: PublicEpgSourceDao
) : PublicEpgSourceRepository {

    override fun getAll(): Flow<List<PublicEpgSource>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun toggle(id: Long, enabled: Boolean) =
        dao.setEnabled(id, enabled)

    override suspend fun addCustom(label: String, url: String) {
        dao.insertAll(listOf(
            PublicEpgSourceEntity(
                label = label,
                region = "Custom",
                url = url,
                isCurated = false,
                isEnabled = true
            )
        ))
    }

    override suspend fun remove(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.delete(entity)
    }

    override suspend fun count(): Int = dao.count()

    override suspend fun insertAll(sources: List<PublicEpgSource>) =
        dao.insertAll(sources.map { it.toEntity() })
}
