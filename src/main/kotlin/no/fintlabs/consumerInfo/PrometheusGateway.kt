package no.fintlabs.consumerInfo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody

@Component
class PrometheusGateway(
    private val prometheusClient: WebClient,
    @Value("\${fint.prometheus.query.memory}") private var memoryQuery: String,
    @Value("\${fint.prometheus.query.restarts}") private var restartsQuery: String,
) {

    // TODO: Handle local fetching of this query

    val log: Logger = LoggerFactory.getLogger(PrometheusGateway::class.java)

    suspend fun getPodInfo(): Response {
        return try {
            prometheusClient.get()
                .uri("/api/v1/query?query={query}", mapOf("query" to memoryQuery))
                .retrieve()
                .awaitBody<Response>()
        } catch (e: WebClientResponseException) {
            log.error("Failed to fetch prometheus data", e)
            throw e
        }
    }

    suspend fun getRestarts(): RestartsInfo {
        try {
            return prometheusClient.get()
                .uri("/api/v1/query?query={query}", mapOf("query" to restartsQuery))
                .retrieve()
                .awaitBody<RestartsInfo>()
        } catch (e: WebClientResponseException) {
            log.error("Failed to fetch prometheus data", e)
            throw e
        }
    }
}