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

    init {
        require(corrId.isNotBlank()) { "corrId must not be null or blank" }
        require(adapterId.isNotBlank()) { "adapterId must not be null or blank" }
        require(orgId.isNotBlank()) { "orgId must not be null or blank" }
        require(domain.isNotBlank()) { "domain must not be null or blank" }
        require(`package`.isNotBlank()) { "package must not be null or blank" }
        require(resource.isNotBlank()) { "resource must not be null or blank" }
        require(syncType.isNotBlank()) { "syncType must not be null or blank" }

        require(totalPages > 0) { "totalPages must be greater than 0" }
        require(pagesAcquired > 0) { "pagesAcquired must be greater than 0" }
        require(totalEntities > 0) { "totalEntities must be greater than 0" }
        require(entitiesAquired > 0) { "entitiesAquired must be greater than 0" }
        require(pages.size > 0) { "pages size must be greater than 0" }
    }

    companion object {
        fun create(syncPageMetadata: SyncPageMetadata, syncType: String): SyncMetadata {
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
            return syncPage.uriRef?.trim('/')?.split("/")?.let {
                Triple(it.getOrNull(0) ?: "", it.getOrNull(1) ?: "", it.getOrNull(2) ?: "")
            } ?: Triple("", "", "")
        }

    }

    fun addPage(sync: SyncMetadata) {
        pages.add(sync.pages[0])
        pagesAcquired += 1
        entitiesAquired += sync.entitiesAquired
        finished = sync.totalPages == pagesAcquired
    }

}
