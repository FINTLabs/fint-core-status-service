package no.novari.status

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.assertEquals

@Testcontainers
class LegacySchemaMigrationTest {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @Test
    fun `a schema with the old status-service tables is baselined and replaced`() {
        val dataSource = DriverManagerDataSource(postgres.jdbcUrl, postgres.username, postgres.password)
        val jdbcClient = JdbcClient.create(dataSource)
        jdbcClient.sql("create table request_fint_event_entity (corr_id varchar(255) primary key)").update()
        jdbcClient.sql("create table response_fint_event_entity (corr_id varchar(255) primary key)").update()
        jdbcClient.sql("create table sync_entity (corr_id varchar(255) primary key, pages jsonb)").update()

        Flyway
            .configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .load()
            .migrate()

        val tables =
            jdbcClient
                .sql(
                    "select table_name from information_schema.tables where table_schema = 'public' and table_name <> 'flyway_schema_history'",
                ).query(String::class.java)
                .list()
                .toSet()
        assertEquals(
            setOf("contract", "contract_mute", "capability", "heartbeat", "sync", "full_sync_status", "sync_page", "event"),
            tables,
        )
    }
}
