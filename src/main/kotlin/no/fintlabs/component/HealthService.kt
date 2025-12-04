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
                    HealthStatus.FOLLOWS_CONTRACT_AND_HAS_HEARTBEAT

                capability.followsContract -> HealthStatus.FOLLOWS_CONTRACT

                !capability.followsContract -> HealthStatus.DO_NOT_FOLLOW_CONTRACT

                !contract.hasContact -> HealthStatus.NO_HEARTBEAT



                else -> HealthStatus.NO_HEALT_STATUS
            }
        }
        return HealthStatus.NO_HEALT_STATUS
    }

    fun getStatus(corrId: String) = syncStatusCache.getSyncStatus(corrId)?.status ?: "No Status Ready"

}