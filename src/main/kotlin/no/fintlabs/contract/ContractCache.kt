package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.adapter.models.sync.SyncPageMetadata
import org.springframework.stereotype.Component

@Component
class ContractCache {

    val cache: MutableMap<String, Contract> = mutableMapOf()

    fun getAll(): MutableCollection<Contract> = cache.values

    fun add(adapterContract: AdapterContract) = cache.put(adapterContract.adapterId, Contract.fromAdapterContract(adapterContract))

    fun save(contract: Contract) = cache.put(contract.adapterId, contract)

    fun updateLastActivity(adapterId: String, time: Long) = cache[adapterId]
        ?.takeIf { time > it.lastActivity }
        ?.apply { lastActivity = time }

    fun updateLastSync(syncPage: SyncPageMetadata) {
        val (domain, pkg, resource) = syncPage.uriRef?.trim('/')!!.split('/')
        cache[syncPage.adapterId]?.let {
            it.getCapability(domain, pkg, resource)!!.updateLastFullSync(syncPage.time)
        }
    }
}