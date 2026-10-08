package no.novari.status.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.io.File

/**
 * Writes the OpenAPI description to the frontend, which generates its API types from it.
 * CI fails when the committed file differs from what the backend produces.
 */
@SpringBootTest(
    properties = [
        "spring.datasource.hikari.schema=public",
        "spring.flyway.default-schema=public",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.bootstrap-servers=localhost:1",
        "fint.status.metrics-initial-delay=PT1H",
    ],
)
@AutoConfigureMockMvc
@Testcontainers
class OpenApiSpecTest {
    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    @Autowired lateinit var mockMvc: MockMvc

    @Test
    fun `write the OpenAPI description for the frontend`() {
        val body =
            mockMvc
                .perform(get("/api/v1/api-docs"))
                .andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
        val mapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build()
        val tree = mapper.readTree(body)
        (tree as ObjectNode).remove("servers")
        val target = File(System.getProperty("openapi.output", "../frontend/app/api/openapi.json"))
        target.parentFile.mkdirs()
        target.writeText(mapper.writeValueAsString(tree) + "\n")
    }
}
