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
                val currentContractStatus = capability.followsContract
                capability.updateFollowsContract()
                compareFollowsContract(currentContractStatus, capability.followsContract)
            }
        }
    }

    private fun compareFollowsContract(old: Boolean, new: Boolean) {
        if (!old && new) {
            syncMetricService.decrementAbsentFullsyncs()
        } else if (old == !new){
            syncMetricService.incrementAbsentFullsyncs()
        }
    }
}