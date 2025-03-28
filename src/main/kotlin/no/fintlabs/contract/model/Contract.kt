package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import java.util.stream.Collectors

data class Contract(
    val adapterId: String,
    val username: String,
    val orgId: String,
    val heartbeatIntervalInMinutes: Int,
    var lastHeartbeat: Number,
    val components: Set<String>,
    var hasContact: Boolean,
    val capabilities: Map<String, Capability>,
    var lastActivity: Long,
) {
    companion object {
        fun fromAdapterContract(adapterContract: AdapterContract): Contract {
            return Contract(
                adapterId = adapterContract.adapterId,
                username = adapterContract.username,
                orgId = adapterContract.orgId,
                heartbeatIntervalInMinutes = adapterContract.heartbeatIntervalInMinutes,
                components = getComponents(adapterContract.capabilities),
                hasContact = false,
                capabilities = createCapabilities(adapterContract.capabilities),
                lastHeartbeat = 0,
                lastActivity = 0
            )
        }

        fun createCapabilities(capabilities: Set<AdapterCapability>): Map<String, Capability> {
            val capabilityMap = mutableMapOf<String, Capability>()
            capabilities.forEach {
                capabilityMap["${it.domainName}.${it.packageName}.${it.resourceName}".lowercase()] = Capability.fromCapability(it)
            }
            return capabilityMap
        }

        private fun getComponents(capabilities: Set<AdapterCapability>): Set<String> =
            capabilities.stream()
                .map { "${it.domainName}.${it.packageName}".lowercase() }
                .collect(Collectors.toSet())
    }

    fun getCapability(domain: String, pkg: String, resource: String): Capability? {
        return capabilities["$domain.$pkg.$resource".lowercase()]
    }

    fun updateLastActivity(newTime: Long): Contract {
        if (newTime > lastActivity) {
            lastActivity = newTime
        }
        return this
    }

}
