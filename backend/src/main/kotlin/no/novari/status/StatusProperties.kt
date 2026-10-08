package no.novari.status

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * enabled = false turns the service off for an environment that does not run fint-core yet:
 * the Kafka listeners do not start, and /api/v1/info tells the frontend to grey it out.
 */
@ConfigurationProperties("fint.status")
data class StatusProperties(
    val enabled: Boolean = true,
    val eventRetention: Duration = Duration.ofDays(30),
    val syncRetention: Duration = Duration.ofDays(30),
)
