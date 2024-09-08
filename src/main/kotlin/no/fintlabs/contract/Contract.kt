package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import java.util.stream.Collectors

data class Contract(
    val adapterId: String,
    val username: String,
    val orgId: String,
    val heartbeatIntervalInMinutes: Int,
    val components: Set<String>,
    var hasContact: Boolean,
    val capabilities: Set<AdapterCapability>
) {
    companion object {
        fun fromAdapterContract(adapterContract: AdapterContract): Contract {
            return Contract(
                adapterId = adapterContract.adapterId,
                username = adapterContract.username,
                orgId = adapterContract.orgId,
                heartbeatIntervalInMinutes = adapterContract.heartbeatIntervalInMinutes,
                components = getComponents(adapterContract.capabilities),
                hasContact = true,
                capabilities = adapterContract.capabilities
            )
        }

        private fun getComponents(capabilities: Set<AdapterCapability>): Set<String> =
            capabilities.stream()
                .map { "${it.domainName}.${it.packageName}".lowercase() }
                .collect(Collectors.toSet())
    }
}
