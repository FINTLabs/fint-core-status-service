package no.fintlabs.sync

import no.fintlabs.sync.model.CustomerResourceSyncStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/sync")
class KundeportalController (
    private val service: KundeportalService
) {

    @GetMapping("/orgId/{orgId}")
    fun getLastSyncByOrgId(orgId: String): ResponseEntity<List<CustomerResourceSyncStatus>>
    = ResponseEntity.ok(service.getLastOnDomain(orgId))
}