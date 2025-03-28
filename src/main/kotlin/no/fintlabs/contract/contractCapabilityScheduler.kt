package no.fintlabs.contract

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ContractCapabilityScheduler(
    private val contractCache: ContractCache
) {

    @Scheduled(cron = "0 * * * * *")
    fun updateFollowsContract() {
        contractCache.getAll().onEach { contract ->
            contract.capabilities.values.forEach { capability ->
                capability.updateFollowsContract()
            }
        }
    }
}