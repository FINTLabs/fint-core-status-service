package no.fintlabs.db

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals

@DataJpaTest(
    properties = [
        "spring.flyway.default-schema=public",
        "spring.datasource.hikari.schema=public",
    ]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SchemaMigrationTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `migrations create the schema the entities expect`() {
        val applied = jdbcTemplate.queryForList(
            "select version from flyway_schema_history where success order by installed_rank",
            String::class.java
        )

        assertEquals(listOf("1"), applied)
    }
}
