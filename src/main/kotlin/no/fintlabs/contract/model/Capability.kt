package no.fintlabs.contract.model

import no.fintlabs.adapter.models.AdapterCapability
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Capability(
    val resourceName: String,
    val fullSyncIntervalInDays: Int,
    val deltaSyncInterval: AdapterCapability.DeltaSyncInterval?,
    var followsContract: Boolean,
    var lastFullSync: Long?,
    var lastFullSyncTime: String?
) {

    companion object {
        fun fromCapability(capability: AdapterCapability): Capability {
            return Capability(
                resourceName = capability.resourceName,
                fullSyncIntervalInDays = capability.fullSyncIntervalInDays,
                deltaSyncInterval = capability.deltaSyncInterval,
                followsContract = true,
                lastFullSync = null,
                lastFullSyncTime = "unknown"
            )
        }
    }

    fun formatLastFullSync(lastFullSync: Long?): String {
        return lastFullSync?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.of("Europe/Oslo"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } ?: "Unknown"
    }

    fun updateLastFullSync(newLastFullSync: Long) {
        if (lastFullSync == null || newLastFullSync > lastFullSync!!) {
            lastFullSync = newLastFullSync
            lastFullSyncTime == formatLastFullSync(newLastFullSync)
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