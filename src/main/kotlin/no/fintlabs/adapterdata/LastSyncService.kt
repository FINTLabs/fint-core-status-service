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
                healty = healthService.calculateHealth(contract, contract.adapterId),
                date = getlastSyncTime(lastDeltas)
            )
        }
    }

    fun findAndCreateLastFull(contract: Contract, component: String): FullSync {
        return FullSync(
            healty = healthService.calculateHealth(contract, component),
            date = getlastSyncTime(
                syncService.getLastFullSync(
                    contract.orgId,
                    component,
                    adapterId = contract.adapterId
                )
            ),
            expectedDate = findNextExpectedFullSync(contract, component)
        )
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