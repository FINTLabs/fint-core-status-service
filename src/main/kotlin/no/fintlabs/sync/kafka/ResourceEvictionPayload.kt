package no.fintlabs.sync.kafka

data class ResourceEvictionPayload(
    val domain: String,
    val pkg: String,
    val org: String,
    val resource: String
)