package no.fintlabs.page

import no.fintlabs.page.model.PageMetadata
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/page-metadata")
class PageMetadataController(
    val pageMetadataCache: PageMetadataCache
) {

    @GetMapping
    fun getAll(): ResponseEntity<Collection<PageMetadata>> =
        ResponseEntity.ok(pageMetadataCache.getAll())

    @GetMapping("/{orgId}")
    fun getAll(@PathVariable orgId: String): ResponseEntity<Collection<PageMetadata>> =
        ResponseEntity.ok(pageMetadataCache.getByOrgId(orgId))

}