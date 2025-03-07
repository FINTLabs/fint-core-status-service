package no.fintlabs.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class PrometheusConfig(
    @Value("\${fint.prometheusUrl}") private val baseUrl: String
) {

    @Bean
    fun prometheusClient(): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}