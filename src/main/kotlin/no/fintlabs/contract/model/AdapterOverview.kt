package no.fintlabs.contract.model

data class AdapterOverview(
    val organzation: String,
    val domain: String,
    val status: Enum<AdapterStatusEnum>
)

data class DomainStatus(
    val component: String,
    val hasContact: Boolean,
    val answersEvents: Boolean,
    val lastDeltaSync: Long,
    val lastFullSync: Long
)

data class ComponentStatus(
    val adapterId: String,
    val heartbeat: Boolean,
    val lastDelta: Long?,
    val lastFull: Long?
)