package no.fintlabs.sync.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata

class SyncMetadata(
    val corrId: String,
    val adapterId: String,
    val orgId: String,
    val domain: String,
    val `package`: String,
    val resource: String,
    val totalPages: Long,
    var pagesAcquired: Long,
    val totalEntities: Long,
    var entitiesAquired: Long,
    val syncType: String,
    val pages: MutableList<Page> = mutableListOf(),
    var finished: Boolean
) {
    companion object {
        fun create(syncPageMetadata: SyncPageMetadata, syncType: String): no.fintlabs.sync.model.SyncMetadata {
            val (domain, `package`, resource) = parseEntityUri(syncPageMetadata)
            return SyncMetadata(
                corrId = syncPageMetadata.corrId ?: "",
                adapterId = syncPageMetadata.adapterId ?: "",
                orgId = syncPageMetadata.orgId ?: "",
                domain = domain,
                `package` = `package`,
                resource = resource,
                totalPages = syncPageMetadata.totalPages,
                pagesAcquired = 1,
                totalEntities = syncPageMetadata.totalSize,
                entitiesAquired = syncPageMetadata.pageSize,
                syncType = syncType,
                pages = mutableListOf(Page.fromSyncPageMetadata(syncPageMetadata)),
                finished = syncPageMetadata.totalPages == 1L
            )
        }

        private fun parseEntityUri(syncPage: SyncPageMetadata): Triple<String, String, String> {
            return syncPage.uriRef?.trim()?.split("/")?.let {
                Triple(it.getOrNull(0) ?: "", it.getOrNull(1) ?: "", it.getOrNull(2) ?: "")
            } ?: Triple("", "", "")
        }

    }

    fun addPage(syncPageMetadata: SyncPageMetadata) {
        pages.add(Page.fromSyncPageMetadata(syncPageMetadata))
        pagesAcquired += 1
        entitiesAquired += syncPageMetadata.pageSize
        finished = syncPageMetadata.totalPages == pagesAcquired
    }

}
