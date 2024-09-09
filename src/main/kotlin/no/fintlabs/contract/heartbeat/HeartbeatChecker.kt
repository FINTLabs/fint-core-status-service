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
        log.info("Checking heartbeats")
        contractCache.getAll().onEach { contract ->
            heartbeatCache.getLastHeartbeat(contract.adapterId)?.let { lastHeartbeat ->
                log.info("Last heartbeat: {}", lastHeartbeat)
                val timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeat
                log.info("Time since last heartbeat: {}", lastHeartbeat)
                val expectedIntervalMillis = contract.heartbeatIntervalInMinutes * 60 * 1000
                log.info("Expected timing: {}", expectedIntervalMillis)

                contract.hasContact = timeSinceLastHeartbeat <= expectedIntervalMillis
                contractCache.save(contract)
            }
        }
    }

}