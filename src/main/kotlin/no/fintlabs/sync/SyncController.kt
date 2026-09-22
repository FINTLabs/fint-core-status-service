package no.fintlabs.sync

import no.fintlabs.sync.model.SyncMetadata
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/page-metadata")
class SyncController(
    val syncService: SyncService
) {

    @GetMapping
    fun get(
        @RequestParam(required = false) from: Long?,
        @RequestParam(required = false) to: Long?
    ): ResponseEntity<Collection<SyncMetadata>> {
        return ResponseEntity.ok(syncService.getByTimeRange(from, to))
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<Collection<SyncMetadata>> =
        ResponseEntity.ok(syncService.getAll())

    @GetMapping("/org/{orgId}")
    fun getByOrg(@PathVariable orgId: String): ResponseEntity<Collection<SyncMetadata>> =
        ResponseEntity.ok(syncService.getByOrgId(orgId))

    @GetMapping("/id/{corrId}")
    fun getByCorrId(@PathVariable corrId: String): ResponseEntity<SyncMetadata> =
        syncService.getByCorrId(corrId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping("/sync-metrics")
    fun getSyncMetrics() = syncService.getMetrics()
}