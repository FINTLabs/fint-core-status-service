package no.novari.status.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.assertTrue

@SpringBootTest(
    properties = [
        "fint.status.enabled=false",
        "spring.datasource.hikari.schema=public",
        "spring.flyway.default-schema=public",
        "spring.kafka.bootstrap-servers=localhost:1",
        "fint.status.metrics-initial-delay=PT1H",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
class DisabledEnvironmentTest {
    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var listeners: KafkaListenerEndpointRegistry

    @Test
    fun `a disabled environment does not start its Kafka listeners and says so`() {
        assertTrue(listeners.listenerContainers.isNotEmpty())
        assertTrue(listeners.listenerContainers.none { it.isRunning })

        mockMvc
            .perform(get("/api/v1/info").with(jwt()))
            .andExpect(jsonPath("$.enabled").value(false))
    }
}
