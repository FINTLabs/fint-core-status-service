package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterContract
import org.springframework.stereotype.Component

@Component
class ContractCache {

    val cache: MutableMap<String, Contract> = mutableMapOf()

    fun getAll(): MutableCollection<Contract> = cache.values

    fun add(adapterContract: AdapterContract) = cache.put(adapterContract.adapterId, Contract.fromAdapterContract(adapterContract))

}