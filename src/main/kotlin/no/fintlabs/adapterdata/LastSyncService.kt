package no.fintlabs.adapterdata

import no.fintlabs.contract.model.Contract
import no.fintlabs.sync.SyncService
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class LastSyncService(
    private val syncService: SyncService,
    private val healthService: HealthService,
) {
    fun findAndCreateLastDelta(contract: Contract, component: String): DeltaSync? {
        val lastDeltas = syncService.getLastDeltaSync(
            contract.orgId,
            component,
            adapterId = contract.adapterId
        )

        val lastDelta = lastDeltas.lastOrNull()

        return lastDelta?.let {
            DeltaSync(
                healty = healthService.getStatus(lastDelta.corrId),
                date = getlastSyncTime(lastDeltas)
            )
        }
    }

    fun findAndCreateLastFull(contract: Contract, component: String): FullSync? {
        val lastSync = syncService.getLastFullSync(
            contract.orgId,
            component,
            adapterId = contract.adapterId
        ).lastOrNull()

        return lastSync?.let {
            FullSync(
                healthService.getStatus(lastSync.corrId),
                date = lastSync.getLastPageTime(),
                findNextExpectedFullSync(contract, component, lastSync.getLastPageTime()).toLong()
            )
        }
    }

    private fun getlastSyncTime(lastSyncs: List<SyncMetadata>): Long {
        if (lastSyncs.isNotEmpty()) {
            return lastSyncs.map {
                it.getLastPageTime()
            }.last()
        } else return 0L
    }

    private fun findNextExpectedFullSync(contract: Contract, component: String, lastSync: Long): Int {
        val fullSyncIntervalInDays = contract.capabilities[component]?.fullSyncIntervalInDays
        val daysSinceLastSync = daysSince(lastSync)
        if (fullSyncIntervalInDays != null && daysSinceLastSync > 0) {
            return fullSyncIntervalInDays - daysSinceLastSync
        }
        return 0
    }

    private fun daysSince(lastSync: Long): Int {
        val then = Instant.ofEpochSecond(lastSync)
        val now = Instant.now()

        val duration = Duration.between(then, now)
        return Math.round(duration.seconds / 86400.0).toInt()
    }

}