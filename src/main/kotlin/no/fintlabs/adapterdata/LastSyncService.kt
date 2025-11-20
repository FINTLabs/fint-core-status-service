package no.fintlabs.adapterdata

import no.fintlabs.contract.model.Contract
import no.fintlabs.sync.SyncService
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service

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
                healty = healthService.calculateHealth(lastDelta.corrId),
                date = getlastSyncTime(lastDeltas)
            )
        } ?: DeltaSync("No status found", 0L)
    }

    fun findAndCreateLastFull(contract: Contract, component: String): FullSync? {
        val lastSync = syncService.getLastFullSync(
            contract.orgId,
            component,
            adapterId = contract.adapterId
        ).lastOrNull()

        return lastSync?.let {
            FullSync(
                healthService.calculateHealth(lastSync.corrId),
                date = lastSync.getLastPageTime(),
                findNextExpectedFullSync(contract, component)
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

    private fun findNextExpectedFullSync(contract: Contract, component: String): Long {
        return 0L
    }

}