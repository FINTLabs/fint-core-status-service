package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterContract
import org.springframework.stereotype.Component

@Component
class AdapterContextCache {

    val cache: MutableMap<String, AdapterContract> = mutableMapOf()

    fun getAll(): MutableCollection<AdapterContract> {
        return cache.values
    }

    fun add(adapterContract: AdapterContract) {
        cache[adapterContract.adapterId] = adapterContract
    }

}