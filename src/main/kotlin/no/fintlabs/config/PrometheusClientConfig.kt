package no.fintlabs.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class PrometheusClientConfig(
    @Value("\${fint.prometheus.baseUrl}") private var baseUrl: String,
) {

    @Bean
    fun prometheusClient(): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}