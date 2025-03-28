package no.fintlabs.contract

import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Component

@Component
class ContractCache {

    val cache: MutableMap<String, Contract> = mutableMapOf()

    fun get(adapterId: String) = cache[adapterId]
    fun getAll(): MutableCollection<Contract> = cache.values
    fun save(contract: Contract) = cache.put(contract.adapterId, contract)

}