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
    val capabilities: Set<AdapterCapability>,
    var lastActivity: Long
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
                capabilities = adapterContract.capabilities,
                lastHeartbeat = 0,
                lastActivity = 0
            )
        }

        private fun getComponents(capabilities: Set<AdapterCapability>): Set<String> =
            capabilities.stream()
                .map { "${it.domainName}.${it.packageName}".lowercase() }
                .collect(Collectors.toSet())
    }
}
