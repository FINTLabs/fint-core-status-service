package no.fintlabs.sync

import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service

@Service
class SyncService (
    private val syncCache: SyncCache
) {
    fun getAll(): List<SyncMetadata> = syncCache.getAll().toList()

    fun getByOrgId(orgId: String): List<SyncMetadata> = syncCache.getByOrgId(orgId).toList()

    fun getByCorrId(id: String): SyncMetadata? = syncCache.getAll().firstOrNull() { it.corrId == id }
}