package no.fintlabs.component

import no.fintlabs.contract.model.Capability
import no.fintlabs.contract.model.Contract
import no.fintlabs.sync.SyncCacheService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ComponentOverWievCache(
    private val syncCacheService: SyncCacheService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val componentOverWievCache:
            MutableMap<String, MutableMap<String, MutableMap<String, MutableList<ComponentOverWiev>>>> = mutableMapOf()

    fun getByOrgAndComponent(
        orgId: String,
        componentName: String
    ): Map<String, List<ComponentOverWiev>> {
        return componentOverWievCache[orgId]?.get(componentName) ?: emptyMap()
    }


    fun add(contract: Contract) {
        val orgId = contract.orgId

        val orgMap = componentOverWievCache.getOrPut(orgId) { mutableMapOf() }

        contract.capabilities.forEach { capability ->
            val componentName = capability.value.componentName
            val resourceName = capability.value.resourceName
            val componentMap = orgMap.getOrPut(componentName) { mutableMapOf() }
            val overview = mapComponentOverWiev(
                capability = capability.value,
                orgId = orgId,
                componentName = componentName,
                adapterId = contract.adapterId,
                hasContact = contract.hasContact
            )
            val listForResource = componentMap.getOrPut(resourceName) { mutableListOf() }
            listForResource += overview
        }
    }


    private fun mapComponentOverWiev(
        capability: Capability,
        orgId: String,
        componentName: String,
        adapterId: String,
        hasContact: Boolean
    ): ComponentOverWiev {

        val lastDelta = syncCacheService
            .getLastDeltaSync(orgId, componentName, adapterId)
            .firstOrNull()
            ?.getLastPageTime()

        val lastFull = syncCacheService
            .getLastFullSync(orgId, componentName, adapterId)
            .firstOrNull()
            ?.getLastPageTime()

        return ComponentOverWiev(
            orgId = orgId,
            componentName = componentName,
            heartbeat = hasContact,
            followsContract = capability.followsContract,
            lastDelta = lastDelta,
            lastFull = lastFull
        )
    }
}

