package no.fintlabs.contract.model

import no.fintlabs.adapter.models.AdapterCapability
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Capability(
    val resourceName: String,
    val componentName: String,
    val fullSyncIntervalInDays: Int,
    val deltaSyncInterval: AdapterCapability.DeltaSyncInterval?,
    var followsContract: Boolean,
    var lastFullSync: Long?
) {

    companion object {
        fun fromCapability(capability: AdapterCapability, componentName: String): Capability {
            return Capability(
                resourceName = capability.resourceName,
                componentName = componentName,
                fullSyncIntervalInDays = capability.fullSyncIntervalInDays,
                deltaSyncInterval = capability.deltaSyncInterval,
                followsContract = true,
                lastFullSync = null,
            )
        }
    }
}