package no.fintlabs.page.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata

class Page(
    val index: Long,
    val pageSize: Long,
    val time: Long
) {
    companion object {
        fun from(pageMetadata: SyncPageMetadata): Page {
            return Page(
                index = pageMetadata.page,
                pageSize = pageMetadata.pageSize,
                time = pageMetadata.time
            )
        }
    }
}
