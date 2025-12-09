package no.fintlabs.contract.model

data class AdapterStatus(
    val organzation: String,
    val domain: String,
    val heartBeat: Boolean,
)

data class DomainStatus(
    val domain: String,
    val hasContact: Boolean,
    val answersEvents: Boolean,
    val lastDeltaSync: Long,
    val lastFullSync: Long
)