package no.fintlabs.sync.model

data class SyncStatus (
    val corrId: String,
    val type: String,
    val status: String
)