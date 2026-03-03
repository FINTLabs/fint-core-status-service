package no.fintlabs.sync

import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service

@Service
class SyncCacheService(
    private val syncCache: SyncCache,
    private val repository: SyncJpaRepository,
) {

    fun getAll(): List<SyncMetadata> = syncCache.getAll().toList()

    fun getByOrgId(orgId: String): List<SyncMetadata> = syncCache.getByOrgId(orgId).toList()

    fun getLastFyllbyAdapterId(adapterId: String): SyncMetadata? =
        syncCache.getAll().firstOrNull { it.adapterId == adapterId && it.syncType.isFull() }

    fun getLastdeltabyAdapterId(adapterId: String): SyncMetadata? =
        syncCache.getAll().firstOrNull { it.adapterId == adapterId && it.syncType.isDelta() }

    fun getByCorrId(id: String): SyncMetadata? = syncCache.getAll().firstOrNull { it.corrId == id }

    fun getByComponent(orgId: String, component: String) =
        getByOrgId(orgId).filter { component == "${it.domain}-${it.`package`}" }

    fun getLastDeltaSync(orgId: String, component: String, adapterId: String) =
        getByComponent(orgId, component).filter { it.syncType.isDelta() && it.adapterId == adapterId }

    fun getLastFullSync(orgId: String, component: String, adapterId: String) =
        getByComponent(orgId, component).filter { it.syncType.isFull() && it.adapterId == adapterId }

    private fun SyncType.isFull() = this == SyncType.FULL

    private fun SyncType.isDelta() = this == SyncType.DELTA

}