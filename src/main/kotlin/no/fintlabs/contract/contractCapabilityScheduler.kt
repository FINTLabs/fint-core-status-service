package no.fintlabs.contract

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class contractCapabilityScheduler (val cache: ContractCache) {

    @Scheduled(cron = "0 * * * * *")
    fun updateFollowsContract() {
        cache.getAll().onEach { contract ->
            contract.capabilities.values.forEach { capability ->
                capability.updateFollowsContract()
            }
        }
    }
}