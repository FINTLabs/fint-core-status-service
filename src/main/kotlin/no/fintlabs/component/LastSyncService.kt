package no.fintlabs.component

import no.fintlabs.contract.model.Contract
import no.fintlabs.sync.SyncCacheService
import no.fintlabs.sync.model.SyncMetadata
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class LastSyncService(
    private val syncCacheService: SyncCacheService,
    private val healthService: HealthService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun findAndCreateLastDelta(contract: Contract, component: String): DeltaSync? {
        val lastDeltas = syncCacheService.getLastDeltaSync(
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
        val lastSync = syncCacheService.getLastFullSync(
            contract.orgId,
            component,
            adapterId = contract.adapterId
        ).lastOrNull()

        return lastSync?.let {
            FullSync(
                healthService.getStatus(lastSync.corrId),
                date = lastSync.getLastPageTime(),
                0L//findNextExpectedFullSync(contract, component, lastSync.getLastPageTime()).toLong()
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

//    private fun findNextExpectedFullSync(contract: Contract, component: String, lastSync: Long): Int {
//        val interval = contract.capabilities[component]?.fullSyncIntervalInDays
//        val daysSinceLastSync = daysSince(lastSync)
//        logger.info("interval: $interval")
//        if (daysSinceLastSync >= interval) {
//            return 0
//        }
//        return interval - daysSinceLastSync
//    }

    private fun daysSince(lastSync: Long): Int {
        val then = Instant.ofEpochSecond(lastSync)
        val now = Instant.now()

        val duration = Duration.between(then, now)
        return Math.round(duration.seconds / 86400.0).toInt()
    }

}