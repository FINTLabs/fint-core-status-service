package no.fintlabs.page.model

import no.fintlabs.adapter.models.sync.SyncType

class PageMetaData(
    val corrId: String,
    val adapterId: String,
    val orgId: String,
    val entityUrl: String,
    val totalPages: Long,
    var pagesAquired: Long,
    val totalEntities: Long,
    var entitiesAquired: Long,
    val syncType: SyncType,
    val pages: MutableList<Page> = mutableListOf()
) {
}