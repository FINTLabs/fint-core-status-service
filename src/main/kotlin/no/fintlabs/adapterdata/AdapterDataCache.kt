package no.fintlabs.adapterdata

import no.fintlabs.contract.ContractCache
import no.fintlabs.contract.model.Contract
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class AdapterDataCache(
    private val adapterDataService: AdapterDataService,
    private val contractCache: ContractCache,
    private val componentDataService: ComponentDataService
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    val adapterDataCache = mutableMapOf<String, MutableList<ComponentData>>()

    fun add(contract: Contract) {
        adapterDataCache[contract.orgId] = componentDataService.createComponentData(contract)
    }

    fun getAll():MutableMap<String,MutableList<ComponentData>> {
        fillCache()
        return adapterDataCache
    }

    fun fillCache() {
        val all = contractCache.getAll()
        all.forEach { add(it) }
    }

}