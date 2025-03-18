package no.fintlabs.sync.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata

class Page(
    val index: Long,
    val pageSize: Long,
    val time: Long
) {
    companion object {
        fun fromSyncPageMetadata(syncPageMetadata: SyncPageMetadata): Page {
            return Page(
                index = syncPageMetadata.page,
                pageSize = syncPageMetadata.pageSize,
                time = syncPageMetadata.time
            )
        }
    }
}
