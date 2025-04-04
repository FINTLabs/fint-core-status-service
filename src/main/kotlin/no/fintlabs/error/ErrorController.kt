package no.fintlabs.error

import no.fintlabs.error.consumer.ConsumerErrorCache
import no.fintlabs.error.provider.ProviderErrorCache
import no.fintlabs.organisationStat.OrganisastionStat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/error")
class ErrorController(
    private val prometheusService: PrometheusService,
    private val consumerErrorCache: ConsumerErrorCache,
    private val providerErrorCache: ProviderErrorCache
) {

    @GetMapping("/consumer")
    fun getConsumerErrors() = consumerErrorCache.getAll()

    @GetMapping("/provider")
    fun getProviderErrors() = providerErrorCache.getAll()

    @GetMapping
    fun errors(): ResponseEntity<MutableMap<String, OrganisastionStat>> {
        return ResponseEntity.ok(prometheusService.getCache())
    }
}