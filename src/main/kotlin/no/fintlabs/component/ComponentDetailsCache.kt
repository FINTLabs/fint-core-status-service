package no.fintlabs.component

import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Service

@Service
class ComponentDetailsCache {
    private val componentDetailsCache: MutableMap<String, List<ComponentDetails>> = mutableMapOf()

    fun add(contract: Contract) {
        componentDetailsCache[contract.orgId] = mapComponentDetails(contract)

    }

    fun getComponentDetails(orgId: String) = componentDetailsCache[orgId]

    private fun mapComponentDetails(contract: Contract): List<ComponentDetails> {
        return contract.components.map { component ->
            ComponentDetails(
                component,
                contract.adapterId,
                contract.hasContact,
                0,
                0

            )
        }
    }
}