package no.fintlabs.event.request

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class RequestFintEventRepository(
    private val requestFintEventJpaRepository: RequestFintEventJpaRepository
) {

    private val log = LoggerFactory.getLogger(RequestFintEventRepository::class.java)

    @Scheduled(cron = "\${event.database.cleanupTime}")
    private fun removeEventsOlderThanTwoWeeks() {
        val days = Instant.now().minus(14, ChronoUnit.DAYS).toEpochMilli()
        try {
            requestFintEventJpaRepository.deleteRowsOlderThan(days)
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }

}
