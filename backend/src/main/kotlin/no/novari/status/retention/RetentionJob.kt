package no.novari.status.retention

import no.novari.status.StatusProperties
import no.novari.status.toOffset
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class RetentionJob(
    private val jdbcClient: JdbcClient,
    private val properties: StatusProperties,
    private val clock: Clock,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${fint.status.retention-cron:0 15 3 * * *}")
    fun deleteExpiredRows() {
        val now = clock.instant()
        val events =
            jdbcClient
                .sql("delete from event where received_at < :cutoff")
                .param("cutoff", now.minus(properties.eventRetention).toOffset())
                .update()
        val syncs =
            jdbcClient
                .sql("delete from sync where started_at < :cutoff")
                .param("cutoff", now.minus(properties.syncRetention).toOffset())
                .update()
        logger.info("Retention deleted {} events and {} syncs", events, syncs)
    }
}
