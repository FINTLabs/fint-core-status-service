package no.fintlabs.component

data class AdapterStatusview(
    val organzation: String,
    val domain: String,
    val status: Enum<HealthStatus>
)

data class ComponentOverWiev(
    val orgId: String,
    val componentName: String,
    val heartbeat: Boolean,
    val followsContract: Boolean,
    val lastDelta: Long?,
    val lastFull: Long?
)

data class ComponentDetails(
    val componentName: String,
    val adapterId: String,
    val heartbeat: Boolean
)
