package no.fintlabs.component


data class ComponentData(
    val componentName: String,
    val adapterId: String,
    val healty: Enum<HealthStatus>,
    val heartBeat: Boolean,
    val lastDelta: DeltaSync?,
    val lastFull: FullSync?
)

data class FullSync(
    val healty: String?,
    val date: Long,
    val expectedNextFullSyncInDays: Long,
)

data class DeltaSync(
    val healty: String?,
    val date: Long
)
