package no.fintlabs.adapterdata

import no.fintlabs.contract.model.Contract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ComponentDataService(
    private val lastSyncService: LastSyncService,
    private val healthService: HealthService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createComponentData(contract: Contract): MutableList<ComponentData> {
        return contract.components.map { component ->
            ComponentData(
                packageName = component,
                healty = healthService.getStatus(contract),
                heartBeat = contract.hasContact,
                lastDelta = lastSyncService.findAndCreateLastDelta(contract, component),
                lastFull = lastSyncService.findAndCreateLastFull(contract, component)
            )
        }.toMutableList()
    }
}