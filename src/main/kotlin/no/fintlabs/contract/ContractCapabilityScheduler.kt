package no.fintlabs.contract

import no.fintlabs.sync.SyncMetricService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ContractCapabilityScheduler(
    private val syncMetricService: SyncMetricService,
    private val contractJpaRepository: ContractJpaRepository
) {

    @Scheduled(cron = "0 * * * * *")
    fun updateFollowsContract() {
        contractJpaRepository.findAll().onEach { contract ->
            contract.capabilities.forEach { capability ->
                capability.updateFollowsContract()
            }
           syncMetricService.publishContractMetrics(contract.capabilities.toList(), contract.orgId)
        }
    }
}