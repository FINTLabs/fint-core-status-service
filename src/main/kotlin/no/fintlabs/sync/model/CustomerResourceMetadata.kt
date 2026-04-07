package no.fintlabs.sync.model

data class CustomerResourceMetadata(
    val corrId: String,
    val adapterId: String,
    val domain: String,
    val `package`: String,
    val resource: String,

    val totalEntries: Long,
    val timeOfLastFullSync: Long,
    val finished: Boolean,
)