package no.fintlabs.contract.heartbeat

import no.fintlabs.contract.ContractJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class HeartbeatChecker(
    val heartbeatCache: HeartbeatCache,
    val contractJpaRepository: ContractJpaRepository
) {

    private val log = LoggerFactory.getLogger(HeartbeatChecker::class.java)

    @Scheduled(fixedRateString = "\${fint.heartbeat.check-rate}")
    fun checkHeartbeats() {
        val nowMillis = System.currentTimeMillis()
        contractJpaRepository.findAll().onEach { contract ->
            val lastHeartbeat = heartbeatCache.getLastHeartbeat(contract.adapterId)
            val expectedIntervalMillis = contract.heartbeatIntervalInMinutes * 60 * 1000L
            contract.hasContact = lastHeartbeat != null && nowMillis - lastHeartbeat <= expectedIntervalMillis
            contractJpaRepository.save(contract)
        }
    }

}