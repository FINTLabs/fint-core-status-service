package no.fintlabs.page

import no.fintlabs.page.model.PageMetaData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/page-metadata")
class PageMetadataController(
    val pageMetadataService: PageMetadataService
) {

    @GetMapping
    fun getAll(): ResponseEntity<Collection<PageMetaData>> =
        ResponseEntity.ok(pageMetadataService.getAll())

    @GetMapping("/{orgId}")
    fun getAll(@PathVariable orgId: String): ResponseEntity<Collection<PageMetaData>> =
        ResponseEntity.ok(pageMetadataService.getAllByOrg(orgId))

}