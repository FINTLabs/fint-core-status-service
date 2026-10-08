package no.novari.status.event

import no.novari.status.health.ContractSnapshot
import no.novari.status.health.ContractSnapshotService
import no.novari.status.web.CountResponse
import no.novari.status.web.PageRequest
import no.novari.status.web.PageResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Duration
import java.time.Instant

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventQueries: EventQueries,
    private val snapshotService: ContractSnapshotService,
    private val clock: Clock,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) status: EventStatusFilter?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(required = false) org: String?,
        @RequestParam(required = false) corrId: String?,
        @RequestParam(required = false) domain: String?,
        @RequestParam(required = false, name = "package") pkg: String?,
        @RequestParam(required = false) resource: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): PageResponse<EventSummary> {
        val request = PageRequest.of(page, size)
        val (rows, total) = eventQueries.find(EventFilter(status, from ?: defaultFrom(), to, org, corrId, domain, pkg, resource), request)
        val snapshot = snapshotService.snapshot()
        return PageResponse(rows.map { it.toSummary(snapshot) }, request.page, request.size, total)
    }

    @GetMapping("/count")
    fun countNew(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: Instant,
        @RequestParam(required = false) status: EventStatusFilter?,
        @RequestParam(required = false) org: String?,
        @RequestParam(required = false) corrId: String?,
        @RequestParam(required = false) domain: String?,
        @RequestParam(required = false, name = "package") pkg: String?,
        @RequestParam(required = false) resource: String?,
    ): CountResponse =
        CountResponse(eventQueries.count(EventFilter(status, since, null, org, corrId, domain, pkg, resource, receivedAfter = since)))

    @GetMapping("/{corrId}")
    fun get(
        @PathVariable corrId: String,
    ): EventDetail {
        val row = eventQueries.get(corrId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No event with corrId $corrId")
        val snapshot = snapshotService.snapshot()
        val candidates =
            if (row.unanswered) {
                snapshot.candidatesFor(row.orgId, row.domainName, row.packageName, row.resourceName).map {
                    Candidate(it.id, it.username, it.orgId, it.heartbeat, it.muted)
                }
            } else {
                emptyList()
            }
        return EventDetail(row.toSummary(snapshot), row.adapterId, candidates)
    }

    private fun defaultFrom(): Instant = clock.instant().minus(Duration.ofDays(1))

    private fun EventRow.toSummary(snapshot: ContractSnapshot) =
        EventSummary(
            corrId = corrId,
            orgId = orgId,
            domainName = domainName,
            packageName = packageName,
            resourceName = resourceName,
            operationType = operationType,
            createdAt = createdAt,
            expiresAt = expiresAt,
            receivedAt = receivedAt,
            handledAt = handledAt,
            status = status,
            reason = reason,
            answeredBy = answeredBy,
            candidates =
                if (unanswered) {
                    snapshot.candidatesFor(orgId, domainName, packageName, resourceName).map { it.username }
                } else {
                    emptyList()
                },
        )
}
