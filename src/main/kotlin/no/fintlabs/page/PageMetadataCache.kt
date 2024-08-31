package no.fintlabs.page

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.page.model.Page
import no.fintlabs.page.model.PageMetaData
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class PageMetadataCache {

    private val cache: MutableMap<String, ConcurrentHashMap<String, PageMetaData>> = ConcurrentHashMap()

    fun add(syncPageMetaData: SyncPageMetadata) {
        val orgId = syncPageMetaData.orgId
        val corrId = syncPageMetaData.corrId

        val corrIdToPageMetaDataMap = cache.computeIfAbsent(orgId) {
            ConcurrentHashMap()
        }

        corrIdToPageMetaDataMap.compute(corrId) { _, existingPageMetaData ->
            val page = Page(
                index = syncPageMetaData.page,
                pageSize = syncPageMetaData.pageSize,
                time = syncPageMetaData.time
            )

            existingPageMetaData?.apply {
                this.pages.add(page)
                this.pagesAquired += 1
                this.entitiesAquired += syncPageMetaData.pageSize
            }
                ?: PageMetaData(
                    corrId = corrId,
                    adapterId = syncPageMetaData.adapterId,
                    orgId = orgId,
                    entityUrl = syncPageMetaData.uriRef,
                    totalPages = syncPageMetaData.totalPages,
                    pagesAquired = 1,
                    totalEntities = syncPageMetaData.totalSize,
                    entitiesAquired = syncPageMetaData.pageSize,
                    syncType = syncPageMetaData.syncType,
                    pages = mutableListOf(page)
                )
        }
    }
}
