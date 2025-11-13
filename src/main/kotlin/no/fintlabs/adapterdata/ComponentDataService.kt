package no.fintlabs.adapterdata

import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Service

@Service
class ComponentDataService(
    private val lastSyncService: LastSyncService,
    private val healthService: HealthService,
) {

    fun createComponentData(contract: Contract): MutableList<ComponentData> {
        var components = mutableListOf<ComponentData>()
        contract.components.forEach { component ->
            ComponentData(
                packageName = component,
                healty = healthService.calculateHealth(contract),
                heartBeat = contract.hasContact,
                lastDelta = lastSyncService.findAndCreateLastDelta(contract, component),
                lastFull = lastSyncService.findAndCreateLastFull(contract, component)
            )
        }
        return components
    }
}