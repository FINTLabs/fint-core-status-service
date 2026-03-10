package no.fintlabs.sync

import no.fintlabs.sync.model.SyncMetadata
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/page-metadata")
class SyncController(
    val syncCacheService: SyncCacheService,
    private val syncCache: SyncCache
) {

    @GetMapping
    fun get(
        @RequestHeader(required = false) from: Long?,
        @RequestHeader(required = false) to: Long?
    ): ResponseEntity<Collection<SyncMetadata>> {
        return ResponseEntity.ok(syncCache.getByTimeRange(from, to))
    }

    @GetMapping("/all")
    fun getAll(): ResponseEntity<Collection<SyncMetadata>> =
        ResponseEntity.ok(syncCache.getAll())

    @GetMapping("/org/{orgId}")
    fun getByOrg(@PathVariable orgId: String): ResponseEntity<Collection<SyncMetadata>> =
        ResponseEntity.ok(syncCacheService.getByOrgId(orgId))

    @GetMapping("/id/{corrId}")
    fun getByCorrId(@PathVariable corrId: String): ResponseEntity<SyncMetadata> =
        syncCacheService.getByCorrId(corrId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
}