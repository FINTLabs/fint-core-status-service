package no.fintlabs.sync

import no.fintlabs.sync.model.SyncMetadata
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/page-metadata")
class SyncController(
    val syncService: SyncService
) {

    @GetMapping
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
}