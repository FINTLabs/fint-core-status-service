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
    val syncCache: SyncCache
) {

    @GetMapping
    fun getAll(): ResponseEntity<Collection<SyncMetadata>> =
        ResponseEntity.ok(syncCache.getAll())

    @GetMapping("/{orgId}")
    fun getAll(@PathVariable orgId: String): ResponseEntity<Collection<SyncMetadata>> =
        ResponseEntity.ok(syncCache.getByOrgId(orgId))

}