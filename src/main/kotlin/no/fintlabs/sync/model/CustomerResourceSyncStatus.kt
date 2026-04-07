package no.fintlabs.sync.model

data class CustomerResourceSyncStatus(
    val adapterId: String,
    val domain: String,
    val `package`: String,
    val resource: String,
    val latestFullSync: CustomerResourceMetadata?,
    val previousCompletedFullSync: CustomerResourceMetadata?,
)
