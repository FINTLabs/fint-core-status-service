package no.fintlabs.contract

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

    fun updateLastFullSync(lastFullSync: Long): Capability {
        return this.apply {
            this.lastFullSync = lastFullSync
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