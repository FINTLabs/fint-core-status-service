package no.fintlabs

import no.fintlabs.event.request.RequestFintEventJpaRepository
import no.fintlabs.event.response.ResponseFintEventJpaRepository
import no.fintlabs.sync.SyncJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RepositoryService(
    private val requestFintEventJpaRepository: RequestFintEventJpaRepository,
    private val responseFintEventJpaRepository: ResponseFintEventJpaRepository,
    private val syncRepository: SyncJpaRepository
) {

    private val log = LoggerFactory.getLogger(RepositoryService::class.java)

    @Scheduled(cron = "\${event.database.cleanupTime}")
    fun removeRowsOlderThanTwoWeeks() {
        val cutoff = Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli()
        try {
            requestFintEventJpaRepository.deleteRowsOlderThan(cutoff)
            responseFintEventJpaRepository.deleteRowsOlderThan(cutoff)
            syncRepository.deleteRowsOlderThan(cutoff)
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }

}
