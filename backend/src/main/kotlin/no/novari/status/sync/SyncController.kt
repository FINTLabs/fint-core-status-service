package no.novari.status.sync

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
@RequestMapping("/api/v1/syncs")
class SyncController(
    private val syncQueries: SyncQueries,
    private val clock: Clock,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "FULL") type: SyncTypeFilter,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(required = false) state: SyncState?,
        @RequestParam(required = false) org: String?,
        @RequestParam(required = false) corrId: String?,
        @RequestParam(required = false) domain: String?,
        @RequestParam(required = false, name = "package") pkg: String?,
        @RequestParam(required = false) resource: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): PageResponse<SyncSummary> =
        syncQueries.find(
            SyncFilter(type.syncType, from ?: defaultFrom(), to, state, org, corrId, domain, pkg, resource),
            PageRequest.of(page, size),
        )

    @GetMapping("/count")
    fun countNew(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: Instant,
        @RequestParam(defaultValue = "FULL") type: SyncTypeFilter,
        @RequestParam(required = false) state: SyncState?,
        @RequestParam(required = false) org: String?,
        @RequestParam(required = false) corrId: String?,
        @RequestParam(required = false) domain: String?,
        @RequestParam(required = false, name = "package") pkg: String?,
        @RequestParam(required = false) resource: String?,
    ): CountResponse =
        CountResponse(
            syncQueries.countStartedAfter(
                SyncFilter(type.syncType, since, null, state, org, corrId, domain, pkg, resource, startedAfter = since),
            ),
        )

    @GetMapping("/{corrId}")
    fun get(
        @PathVariable corrId: String,
    ): SyncDetail = syncQueries.get(corrId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No sync with corrId $corrId")

    private fun defaultFrom(): Instant = clock.instant().minus(Duration.ofDays(7))
}

enum class SyncTypeFilter(
    val syncType: SyncType?,
) {
    FULL(SyncType.FULL),
    DELTA(SyncType.DELTA),
    DELETE(SyncType.DELETE),
    ALL(null),
}
