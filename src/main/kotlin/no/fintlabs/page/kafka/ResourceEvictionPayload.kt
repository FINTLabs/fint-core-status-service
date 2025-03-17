package no.fintlabs.page.kafka

import no.fintlabs.page.model.PageMetadata

data class ResourceEvictionPayload(
    val domain: String,
    val pkg: String,
    val resource: String
) {
    companion object {
        fun fromPageMetadata(pageMetadata: PageMetadata): ResourceEvictionPayload {
            return ResourceEvictionPayload(pageMetadata.domain, pageMetadata.`package`, pageMetadata.resource)
        }
    }
}