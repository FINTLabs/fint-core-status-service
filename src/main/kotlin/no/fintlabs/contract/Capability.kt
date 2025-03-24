package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterCapability
import java.time.Duration
import java.time.Instant

data class Capability(
    val fullSyncIntervalInDays: Int,
    val deltaSyncInterval: AdapterCapability.DeltaSyncInterval?,
    var followsContract: Boolean,
    var lastFullSync: Long
) {

    companion object {
        fun fromCapability(capability: AdapterCapability): Capability {
            return Capability(
                fullSyncIntervalInDays = capability.fullSyncIntervalInDays,
                deltaSyncInterval = capability.deltaSyncInterval,
                followsContract = false,
                lastFullSync = 0L
            )
        }
    }

    fun updateLastFullSync(lastFullSync: Long): Capability {
        return this.apply {
            this.lastFullSync = lastFullSync
        }
    }

    fun updateFollowsContract(){
        val daysSinceLastFullSync = Duration.between(
            Instant.ofEpochMilli(this.lastFullSync),
            Instant.now()
        ).toDays()
        this.followsContract = if (this.lastFullSync == 0L) {
            false
        } else {
            daysSinceLastFullSync <= this.fullSyncIntervalInDays
        }
    }
}