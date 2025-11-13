package no.fintlabs.adapterdata

import com.nimbusds.jose.util.health.HealthStatus

data class ComponentData(
    val packageName: String,
    val healty: Enum<HealthStatus>,
    val heartBeat: Boolean,
    val lastDelta: DeltaSync?,
    val lastFull: FullSync?
)

data class FullSync(
    val healty: Boolean,
    val date: Long,
    val expectedDate: Long,
)

data class DeltaSync(
    val healty: Boolean,
    val date: Long
)
