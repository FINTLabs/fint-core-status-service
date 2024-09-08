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
        val contracts = contractCache.getAll()

        contracts.forEach { contract ->
            val lastHeartbeat = heartbeatCache.getLastHeartbeat(contract.adapterId)
            if (lastHeartbeat != null) {
                val timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeat
                val expectedIntervalMillis = contract.heartbeatIntervalInMinutes * 60 * 1000

                if (timeSinceLastHeartbeat > expectedIntervalMillis) {
                    contract.hasContact = false
                    contractCache.save(contract)
                } else {
                    contract.hasContact = true
                    contractCache.save(contract)
                }
            }
        }
    }

}