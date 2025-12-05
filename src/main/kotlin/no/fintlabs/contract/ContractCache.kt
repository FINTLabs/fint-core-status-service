package no.fintlabs.contract

import no.fintlabs.contract.model.Contract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ContractCache() {

    val cache: MutableMap<String, Contract> = mutableMapOf()

    fun get(adapterId: String) = cache[adapterId]
    fun getAll(): MutableCollection<Contract> = cache.values
    fun getByOrgId(orgId: String): List<Contract> = cache.values.filter { it.orgId == orgId }
    fun save(contract: Contract) {
        cache[contract.adapterId] = contract
    }

    fun getByOrgAndComponent(orgId: String, component: String): MutableList<Contract> {
        var contracts = mutableListOf<Contract>()
        for (contract in getByOrgId(orgId)) {
            if (contract.components.contains(component))
                contracts.add(contract)
        }
        return contracts
    }

}