package no.fintlabs.sync

import no.fintlabs.sync.model.SyncStatus
import org.springframework.stereotype.Service

@Service
class SyncStatusCache {

    private val cache: MutableMap<String, SyncStatus> = mutableMapOf()

    fun addSyncStatus(syncStatus: SyncStatus) {
        cache[syncStatus.corrId] = syncStatus
    }

    fun getSyncStatus(corrId: String): SyncStatus? {
        return cache[corrId]
    }


}