package no.fintlabs.sync.kafka

data class ResourceEvictionPayload(
    val domain: String,
    val pkg: String,
    val resource: String,
    val org: String
)