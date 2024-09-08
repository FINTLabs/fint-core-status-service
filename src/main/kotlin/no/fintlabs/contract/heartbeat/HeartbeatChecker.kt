package no.fintlabs.contract.heartbeat

import no.fintlabs.contract.ContractCache
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class HeartbeatChecker(
    val heartbeatCache: HeartbeatCache,
    val contractCache: ContractCache
) {

    @Scheduled(fixedRateString = "\${heartbeat.check.rate}")
    fun checkHeartbeats() {
        contractCache.getAll().onEach { contract ->
            heartbeatCache.getLastHeartbeat(contract.adapterId)?.let { lastHeartbeat ->
                val timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeat
                val expectedIntervalMillis = contract.heartbeatIntervalInMinutes * 60 * 1000

                contract.hasContact = timeSinceLastHeartbeat <= expectedIntervalMillis
                contractCache.save(contract)
            }
        }
    }

}