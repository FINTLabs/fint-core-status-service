package no.fintlabs.errors

import kotlinx.coroutines.reactor.awaitSingle
import no.fintlabs.model.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PrometheusGateway(
    private val prometheusClient: WebClient,
    @Value("\${fint.prometheus.query}") private var query: String,
) {

    val log: Logger = LoggerFactory.getLogger(PrometheusGateway::class.java)

    suspend fun fetchPrometheusData(): Response {
        return try {
            log.info("Fetching prometheus data... {}", query)
            prometheusClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(query)
                .retrieve()
                .bodyToMono(Response::class.java)
                .awaitSingle()
        } catch (e: WebClientResponseException) {
            log.error("Failed to fetch prometheus data", e)
            throw e
        }
    }
}