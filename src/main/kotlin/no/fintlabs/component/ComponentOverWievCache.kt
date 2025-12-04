package no.fintlabs.component

import no.fintlabs.component.dto.ComponentDto
import no.fintlabs.component.dto.ComponentsResponse
import no.fintlabs.component.dto.ResourceDto
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
            MutableMap<String, MutableMap<String, MutableMap<String, MutableList<ComponentOverWiev>>>> =
        mutableMapOf()

    fun getByOrgAndComponent(
        orgId: String,
        componentName: String
    ): ComponentsResponse {
        val orgMap = componentOverWievCache[orgId] ?: return ComponentsResponse(emptyList())
        val componentMap = orgMap[componentName] ?: return ComponentsResponse(emptyList())

        val resources = componentMap.map { (resourceName, overviewList) ->
            val merged = mergeOverviews(overviewList)

            ResourceDto(
                resourceName = resourceName,
                heartbeat = merged.heartbeat,
                followsContract = merged.followsContract,
                lastDelta = merged.lastDelta,
                lastFull = merged.lastFull
            )
        }.sortedBy { it.resourceName }

        val componentDto = ComponentDto(
            orgId = orgId,
            component = componentName,
            resources = resources
        )

        return ComponentsResponse(components = listOf(componentDto))
    }

    private fun mergeOverviews(
        list: List<ComponentOverWiev>
    ): ComponentOverWiev {
        require(list.isNotEmpty()) { "Cannot merge empty list of ComponentOverWiev" }

        return list.reduce { acc, next ->
            ComponentOverWiev(
                orgId = acc.orgId,
                componentName = acc.componentName,
                heartbeat = acc.heartbeat || next.heartbeat,
                followsContract = acc.followsContract && next.followsContract,
                lastDelta = listOfNotNull(acc.lastDelta, next.lastDelta).maxOrNull(),
                lastFull = listOfNotNull(acc.lastFull, next.lastFull).maxOrNull()
            )
        }
    }

    fun add(contract: Contract) {
        val orgId = contract.orgId
        val adapterId = contract.adapterId
        val hasContact = contract.hasContact

        logger.info("Mapping contract for orgId=$orgId, adapterId=$adapterId")

        val orgMap = componentOverWievCache.getOrPut(orgId) { mutableMapOf() }

        contract.capabilities.values.forEach { capability ->
            val componentName = capability.componentName
            val resourceName = capability.resourceName

            val componentMap = orgMap.getOrPut(componentName) { mutableMapOf() }

            val overview = mapComponentOverWiev(
                orgId = orgId,
                adapterId = adapterId,
                componentName = componentName,
                hasContact = hasContact,
                followsContract = capability.followsContract
            )

            val listForResource = componentMap.getOrPut(resourceName) { mutableListOf() }
            listForResource += overview
        }
    }

    private fun mapComponentOverWiev(
        orgId: String,
        adapterId: String,
        componentName: String,
        hasContact: Boolean,
        followsContract: Boolean
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
            followsContract = followsContract,
            lastDelta = lastDelta,
            lastFull = lastFull
        )
    }
}
