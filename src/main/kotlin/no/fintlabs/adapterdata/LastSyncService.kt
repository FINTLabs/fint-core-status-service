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
                date = lastDelta.getLastPageTime()
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

    private fun getlastSyncTime(lastFullSync: List<SyncMetadata>): Long {
        return lastFullSync.map {
            it.getLastPageTime()
        }.last()
    }

    private fun findNextExpectedFullSync(contract: Contract, component: String): Long {
        return 0L
    }

}