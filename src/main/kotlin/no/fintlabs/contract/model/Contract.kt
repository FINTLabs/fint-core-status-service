package no.fintlabs.contract.model

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract

data class Contract(
    val adapterId: String,
    val username: String,
    val orgId: String,
    val heartbeatIntervalInMinutes: Int,
    var lastHeartbeat: Long,
    var lastActivity: Long,
    val components: Set<String>,
    var hasContact: Boolean,
    val capabilities: Map<String, Capability>,
) {

    fun updateLastActivity(newTime: Long) =
        takeIf { newTime > lastActivity }?.let { lastActivity = newTime }


    companion object {
        fun fromAdapterContract(adapterContract: AdapterContract): Contract {
            return Contract(
                adapterId = adapterContract.adapterId,
                username = adapterContract.username,
                orgId = adapterContract.orgId,
                heartbeatIntervalInMinutes = adapterContract.heartbeatIntervalInMinutes,
                lastHeartbeat = 0,
                lastActivity = 0,
                components = getComponents(adapterContract.capabilities).toSet(),
                hasContact = true,
                capabilities = createCapabilities(adapterContract.capabilities),
            )
        }


        private fun getComponents(capabilities: MutableSet<AdapterCapability>): List<String> {
            return capabilities.map { capabilities ->
                capabilities.component
            }
        }

        private fun createCapabilities(capabilities: Set<AdapterCapability>): Map<String, Capability> {
            val capabilityMap = mutableMapOf<String, Capability>()
            capabilities.forEach {
                capabilityMap["${it.domainName}.${it.packageName}.${it.resourceName}".lowercase()] =
                    Capability.fromCapability(it, "${it.domainName}-${it.packageName}")
            }
            return capabilityMap
        }

    }

    fun getCapabilities() = capabilities.values

    fun getCapability(domain: String, pkg: String, resource: String): Capability? =
        capabilities["$domain.$pkg.$resource".lowercase()]

}
