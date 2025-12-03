package no.fintlabs.component

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdapterDataService(
) {
    private val logger = LoggerFactory.getLogger(javaClass)

//    fun createAdapterData(contract: Contract): AdapterData {
//        val componentData = componentDataService.createComponentData(contract)
//        val orgMap: MutableMap<String, MutableList<ComponentData>> =
//            mutableMapOf(contract.orgId to componentData)
//        return AdapterData(organization = orgMap)
//    }
}