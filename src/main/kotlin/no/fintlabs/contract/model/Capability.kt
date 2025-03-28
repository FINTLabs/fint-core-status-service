package no.fintlabs.contract.model

import no.fintlabs.adapter.models.AdapterCapability
import java.time.Duration
import java.time.Instant

data class Capability(
    val fullSyncIntervalInDays: Int,
    val deltaSyncInterval: AdapterCapability.DeltaSyncInterval?,
    var followsContract: Boolean,
    var lastFullSync: Long?
) {

    companion object {
        fun fromCapability(capability: AdapterCapability): Capability {
            return Capability(
                fullSyncIntervalInDays = capability.fullSyncIntervalInDays,
                deltaSyncInterval = capability.deltaSyncInterval,
                followsContract = true,
                lastFullSync = null
            )
        }
    }

    fun updateLastFullSync(newLastFullSync: Long) {
        if (this.lastFullSync == null || newLastFullSync > this.lastFullSync!!) {
            this.lastFullSync = newLastFullSync
        }
    }

    fun updateFollowsContract() {
        if (lastFullSync == null) {
            followsContract = false
            return
        }
        val daysSinceLastFullSync = Duration.between(
            Instant.ofEpochMilli(lastFullSync!!),
            Instant.now()
        ).toDays()
        followsContract = daysSinceLastFullSync <= fullSyncIntervalInDays
    }
}