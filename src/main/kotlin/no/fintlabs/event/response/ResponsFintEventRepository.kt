package no.fintlabs.event.response

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ResponsFintEventRepository(
    private val responseFintEventJpaRepository: ResponseFintEventJpaRepository
) {

    private val log = LoggerFactory.getLogger(ResponsFintEventRepository::class.java)

    @Scheduled(cron = "\${event.database.cleanupTime}")
    private fun removeEventsOlderThanTwoWeeks() {
        val days = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()
        try {
            responseFintEventJpaRepository.deleteRowsOlderThan(days)
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }

}
