package no.fintlabs.contract.model

import no.fintlabs.adapter.models.AdapterCapability
import java.time.Duration
import java.time.Instant

data class Capability(
    val resourceName: String,
    val fullSyncIntervalInDays: Int,
    val deltaSyncInterval: AdapterCapability.DeltaSyncInterval?,
    var followsContract: Boolean,
    var lastFullSync: Long?
) {

    companion object {
        fun fromCapability(capability: AdapterCapability): Capability {
            return Capability(
                resourceName = capability.resourceName,
                fullSyncIntervalInDays = capability.fullSyncIntervalInDays,
                deltaSyncInterval = capability.deltaSyncInterval,
                followsContract = true,
                lastFullSync = null
            )
        }
    }

    fun updateLastFullSync(newLastFullSync: Long) {
        if (lastFullSync == null || newLastFullSync > lastFullSync!!) {
            lastFullSync = newLastFullSync
        }
    }

    fun updateFollowsContract() {
        followsContract = if (lastFullSync != null) {
            lastFullSync!!.getDaysSinceNow() <= fullSyncIntervalInDays
        } else false
    }

    private fun Long.getDaysSinceNow(): Long =
        this.let { Duration.between(Instant.ofEpochMilli(it), Instant.now()) }.toDays()

}