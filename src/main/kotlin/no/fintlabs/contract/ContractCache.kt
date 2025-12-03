package no.fintlabs.contract

import no.fintlabs.component.AdapterStatusCache
import no.fintlabs.component.ComponentDetailsCache
import no.fintlabs.component.ComponentOverWievCache
import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Component

@Component
class ContractCache(
    private val adapterDataCache: AdapterStatusCache,
    private val componentDetailsCache: ComponentDetailsCache,
    private val componentOverWievCache: ComponentOverWievCache
) {

    val cache: MutableMap<String, Contract> = mutableMapOf()

    fun get(adapterId: String) = cache[adapterId]
    fun getAll(): MutableCollection<Contract> = cache.values
    fun save(contract: Contract){
        cache[contract.adapterId] = contract
        adapterDataCache.add(contract)
        componentDetailsCache.add(contract)
        componentOverWievCache.add(contract)
    }

}