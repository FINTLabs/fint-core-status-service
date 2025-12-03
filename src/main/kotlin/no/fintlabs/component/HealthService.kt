package no.fintlabs.component

import no.fintlabs.contract.model.Contract
import no.fintlabs.sync.SyncStatusCache
import org.springframework.stereotype.Service

@Service
class HealthService(
    private val syncStatusCache: SyncStatusCache,
) {

    fun getStatus(contract: Contract): HealthStatus {
        for (capability in contract.capabilities.values) {
            return when {
                capability.followsContract && contract.hasContact ->
                    HealthStatus.FOLLOWS_CONTRACT

                !capability.followsContract && !contract.hasContact ->
                    HealthStatus.DONT_HAVE_CONTACT_AND_DOES_NOT_FOLLOW_CONTRACT

                capability.followsContract ->
                    HealthStatus.FOLLOWS_CONTRACT

                else -> HealthStatus.NO_STATUS_FOUND
            }
        }
        return HealthStatus.NO_STATUS_FOUND
    }

    fun getStatus(corrId: String) = syncStatusCache.getSyncStatus(corrId)?.status ?: "No Status Ready"

}