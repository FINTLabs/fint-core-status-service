package no.novari.status

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("fint.status")
data class StatusProperties(
    val eventRetention: Duration = Duration.ofDays(30),
    val syncRetention: Duration = Duration.ofDays(30),
)
