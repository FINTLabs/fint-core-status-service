package no.fintlabs.adapterdata

import com.nimbusds.jose.util.health.HealthStatus
import no.fintlabs.contract.model.Contract
import no.fintlabs.sync.SyncStatusCache
import org.springframework.stereotype.Service

@Service
class HealthService(
    private val syncStatusCache: SyncStatusCache,
) {

    fun calculateHealth(contract: Contract): Enum<HealthStatus> {
        return HealthStatus.HEALTHY
    }

    fun calculateHealth(corrId: String) = syncStatusCache.getSyncStatus(corrId)?.status ?: "No Status Ready"

}