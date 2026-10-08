package no.novari.status.event

import no.novari.status.health.HeartbeatHealth
import java.time.Instant

enum class EventStatusFilter(
    val statuses: List<EventStatus>,
) {
    PENDING(listOf(EventStatus.PENDING)),
    EXPIRED(listOf(EventStatus.EXPIRED)),
    ANSWERED(listOf(EventStatus.ANSWERED)),
    ERRORS(listOf(EventStatus.FAILED, EventStatus.REJECTED, EventStatus.CONFLICTED)),
}

data class EventFilter(
    val status: EventStatusFilter?,
    val from: Instant,
    val to: Instant?,
    val orgId: String?,
    val corrId: String?,
    val domainName: String?,
    val packageName: String?,
    val resourceName: String?,
    val receivedAfter: Instant? = null,
)

data class EventSummary(
    val corrId: String,
    val orgId: String,
    val domainName: String?,
    val packageName: String?,
    val resourceName: String?,
    val operationType: String?,
    val createdAt: Instant?,
    val expiresAt: Instant?,
    val receivedAt: Instant,
    val handledAt: Instant?,
    val status: EventStatus,
    val reason: String?,
    val answeredBy: String?,
    val candidates: List<String>,
)

data class Candidate(
    val contractId: Long,
    val username: String,
    val orgId: String,
    val heartbeat: HeartbeatHealth,
    val muted: Boolean,
)

data class EventDetail(
    val event: EventSummary,
    val adapterId: String?,
    val candidates: List<Candidate>,
)
