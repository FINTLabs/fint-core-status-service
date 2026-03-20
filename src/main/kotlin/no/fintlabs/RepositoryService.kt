package no.fintlabs

import no.fintlabs.event.response.ResponsFintEventRepository
import no.fintlabs.event.response.ResponseFintEventJpaRepository
import no.fintlabs.sync.SyncJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RepositoryService(
    private val responseFintEventJpaRepository: ResponseFintEventJpaRepository,
    private val syncRepository: SyncJpaRepository
) {


    private val log = LoggerFactory.getLogger(ResponsFintEventRepository::class.java)

    @Scheduled(cron = "\${event.database.cleanupTime}")
    private fun removeEventsOlderThanTwoWeeks() {
        val days = Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli()
        try {
            responseFintEventJpaRepository.deleteRowsOlderThan(days)
            syncRepository.deleteRowsOlderThan(days)
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }

}
