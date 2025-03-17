package no.fintlabs.page.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.adapter.models.sync.SyncType

class PageMetadata(
    val corrId: String,
    val adapterId: String,
    val orgId: String,
    val entityUrl: String,
    val totalPages: Long,
    var pagesAcquired: Long,
    val totalEntities: Long,
    var entitiesAquired: Long,
    val syncType: String,
    val pages: MutableList<Page> = mutableListOf(),
    var finished: Boolean
) {
    companion object {
        fun create(pageMetadata: SyncPageMetadata, syncType: String): PageMetadata {
            return PageMetadata(
                corrId = pageMetadata.corrId ?: "",
                adapterId = pageMetadata.adapterId ?: "",
                orgId = pageMetadata.orgId ?: "",
                entityUrl = pageMetadata.uriRef ?: "",
                totalPages = pageMetadata.totalPages,
                pagesAcquired = 1,
                totalEntities = pageMetadata.totalSize,
                entitiesAquired = pageMetadata.pageSize,
                syncType = syncType,
                pages = mutableListOf(Page.from(pageMetadata)),
                finished = pageMetadata.totalPages == 1L
            )
        }
    }

    fun addPage(pageMetadata: SyncPageMetadata) {
        pages.add(Page.from(pageMetadata))
        pagesAcquired += 1
        entitiesAquired += pageMetadata.pageSize
        finished = pageMetadata.totalPages == pagesAcquired
    }

}
