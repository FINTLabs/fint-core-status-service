package no.fintlabs.page.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.adapter.models.sync.SyncType

class PageMetaData(
    val corrId: String,
    val adapterId: String,
    val orgId: String,
    val entityUrl: String,
    val totalPages: Long,
    var pagesAcquired: Long,
    val totalEntities: Long,
    var entitiesAquired: Long,
    val syncType: SyncType,
    val pages: MutableList<Page> = mutableListOf()
) {
    companion object {
        fun create(pageMetadata: SyncPageMetadata, syncType: SyncType): PageMetaData {
            val initialPage = Page(
                index = pageMetadata.page,
                pageSize = pageMetadata.pageSize,
                time = pageMetadata.time
            )
            return PageMetaData(
                corrId = pageMetadata.corrId ?: "",
                adapterId = pageMetadata.adapterId ?: "",
                orgId = pageMetadata.orgId ?: "",
                entityUrl = pageMetadata.uriRef ?: "",
                totalPages = pageMetadata.totalPages,
                pagesAcquired = 1,
                totalEntities = pageMetadata.totalSize,
                entitiesAquired = pageMetadata.pageSize,
                syncType = syncType,
                pages = mutableListOf(initialPage)
            )
        }

        fun update(existing: PageMetaData, pageMetadata: SyncPageMetadata): PageMetaData {
            val newPage = Page(
                index = pageMetadata.page,
                pageSize = pageMetadata.pageSize,
                time = pageMetadata.time
            )
            existing.pages.add(newPage)
            existing.pagesAcquired += 1
            existing.entitiesAquired += pageMetadata.pageSize
            return existing
        }
    }
}
