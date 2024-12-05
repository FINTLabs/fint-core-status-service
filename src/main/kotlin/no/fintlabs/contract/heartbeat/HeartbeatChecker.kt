package no.fintlabs.contract.heartbeat

import no.fintlabs.contract.ContractCache
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class HeartbeatChecker(
    val heartbeatCache: HeartbeatCache,
    val contractCache: ContractCache
) {

    private val log = LoggerFactory.getLogger(HeartbeatChecker::class.java)

    @Scheduled(fixedRateString = "\${fint.heartbeat.check-rate}")
    fun checkHeartbeats() {
        contractCache.getAll().onEach { contract ->
            heartbeatCache.getLastHeartbeat(contract.adapterId)?.let { lastHeartbeat ->
                val timeInSeconds = System.currentTimeMillis() / 1000
                val timeSinceLastHeartbeat = timeInSeconds - lastHeartbeat
                val expectedIntervalSeconds = contract.heartbeatIntervalInMinutes * 60

                if (timeSinceLastHeartbeat <= expectedIntervalSeconds) {
                    contract.hasContact = true
                }else if(timeSinceLastHeartbeat >= expectedIntervalSeconds){
                    contract.hasContact = false
                }

                contractCache.save(contract)
            }
        }
    }

}