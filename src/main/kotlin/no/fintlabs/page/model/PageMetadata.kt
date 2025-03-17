package no.fintlabs.page.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata

class PageMetadata(
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
        fun create(pageMetadata: SyncPageMetadata, syncType: String): PageMetadata {
            val (domain, `package`, resource) = parseEntityUri(pageMetadata)
            return PageMetadata(
                corrId = pageMetadata.corrId ?: "",
                adapterId = pageMetadata.adapterId ?: "",
                orgId = pageMetadata.orgId ?: "",
                domain = domain,
                `package` = `package`,
                resource = resource,
                totalPages = pageMetadata.totalPages,
                pagesAcquired = 1,
                totalEntities = pageMetadata.totalSize,
                entitiesAquired = pageMetadata.pageSize,
                syncType = syncType,
                pages = mutableListOf(Page.from(pageMetadata)),
                finished = pageMetadata.totalPages == 1L
            )
        }

        private fun parseEntityUri(syncPage: SyncPageMetadata): Triple<String, String, String> {
            return syncPage.uriRef?.trim()?.split("/")?.let {
                Triple(it.getOrNull(0) ?: "", it.getOrNull(1) ?: "", it.getOrNull(2) ?: "")
            } ?: Triple("", "", "")
        }
    }

    fun addPage(pageMetadata: SyncPageMetadata) {
        pages.add(Page.from(pageMetadata))
        pagesAcquired += 1
        entitiesAquired += pageMetadata.pageSize
        finished = pageMetadata.totalPages == pagesAcquired
    }

}
