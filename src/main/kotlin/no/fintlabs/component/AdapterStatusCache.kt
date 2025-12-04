package no.fintlabs.component

import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Service

@Service
class AdapterStatusCache{

    private val adapterStatusCache: MutableSet<AdapterStatusview> = mutableSetOf()


    fun add(contract: Contract) {
        adapterStatusCache.add(mapAdapterData(contract))
    }

    fun getAdapterStatus() = adapterStatusCache

    private fun mapAdapterData(contract: Contract): AdapterStatusview {
        return AdapterStatusview(
            contract.orgId,
            getDomain(contract),
            AdapterStatus.OK
        )
    }

    fun getDomain(contract: Contract): String {
        return contract.components.map { component ->
            component.substringBefore("-")
        }.first()
    }

}