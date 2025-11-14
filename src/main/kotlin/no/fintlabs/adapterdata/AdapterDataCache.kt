package no.fintlabs.adapterdata

import no.fintlabs.contract.ContractCache
import no.fintlabs.contract.model.Contract
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class AdapterDataCache(
    private val adapterDataService: AdapterDataService,
    private val contractCache: ContractCache
) {

    val adapterDataCache = mutableListOf<AdapterData>()

    fun add(contract: Contract) {
        adapterDataCache.add(adapterDataService.createAdapterData(contract))
    }

    fun getAll():MutableList<AdapterData> {
        return adapterDataCache
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    fun fillCache() {
        val all = contractCache.getAll()
        all.forEach { add(it) }
    }

}