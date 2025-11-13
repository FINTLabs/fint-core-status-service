package no.fintlabs.adapterdata

import no.fintlabs.contract.ContractCache
import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Service

@Service
class AdapterDataCache(
    private val adapterDataService: AdapterDataService,
    private val contractCache: ContractCache
) {

    val adapterDataCache = mutableMapOf<String, MutableList<AdapterData>>()

    fun add(contract: Contract) {
        adapterDataCache[contract.orgId] = mutableListOf(adapterDataService.createAdapterData(contract))
    }

    fun getAll(): MutableCollection<MutableList<AdapterData>> {
        fillCache()
        return adapterDataCache.values
    }

    fun fillCache() {
        val all = contractCache.getAll()
        all.forEach { add(it) }
    }

}