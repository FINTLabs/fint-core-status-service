package no.fintlabs.error

import no.fintlabs.organisationStat.OrganisastionStat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/errors")
class ErrorController(
    private val prometheusService: PrometheusService
) {

    @GetMapping
    fun errors(): ResponseEntity<MutableMap<String, OrganisastionStat>> {
        return ResponseEntity.ok(prometheusService.getCache())
    }
}