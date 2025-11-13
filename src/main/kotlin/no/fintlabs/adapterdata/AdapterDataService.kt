package no.fintlabs.adapterdata

import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Service

@Service
class AdapterDataService(
    private val componentDataService: ComponentDataService
) {

    fun createAdapterData(contract: Contract): AdapterData {
        val map = mutableMapOf<String, MutableList<ComponentData>>()
        val componentData = componentDataService.createComponentData(contract)
        map.getOrPut(contract.orgId) { mutableListOf() }
            .addAll(componentData)
        return AdapterData(map)
    }
}