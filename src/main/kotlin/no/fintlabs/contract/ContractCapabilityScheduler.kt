package no.fintlabs.contract

import no.fintlabs.sync.SyncMetricService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ContractCapabilityScheduler(
    private val contractCache: ContractCache,
    private val syncMetricService: SyncMetricService
) {

    @Scheduled(cron = "0 * * * * *")
    fun updateFollowsContract() {
        contractCache.getAll().onEach { contract ->
            contract.getCapabilities().forEach { capability ->
                capability.updateFollowsContract()
            }
            syncMetricService.publishContractMetrics(contract.capabilities.values.toList(), contract.orgId)
        }
    }
}