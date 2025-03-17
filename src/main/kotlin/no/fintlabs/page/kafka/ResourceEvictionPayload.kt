package no.fintlabs.page.kafka

data class ResourceEvictionPayload(
    val domain: String,
    val pkg: String,
    val resource: String
)