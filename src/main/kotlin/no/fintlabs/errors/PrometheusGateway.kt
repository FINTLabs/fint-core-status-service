package no.fintlabs.errors


import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PrometheusGateway(
    @Value("\${fint.prometheusUrl}") private val url: String,
    val RestClient: RestClient
) {

    fun fetchPrometheusData(restClient: RestClient): RestClient {
        restClient.get().uri("$url/prometheus")

    }

}