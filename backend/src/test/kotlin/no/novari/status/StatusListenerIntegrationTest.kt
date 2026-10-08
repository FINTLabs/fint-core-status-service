package no.novari.status

import com.fasterxml.jackson.databind.ObjectMapper
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.adapter.models.AdapterHeartbeat
import no.fintlabs.adapter.models.EventCapability
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.adapter.operation.OperationType
import no.novari.status.kafka.StatusTopics
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(
    properties = [
        "spring.kafka.security.protocol=PLAINTEXT",
        "spring.datasource.hikari.schema=public",
        "spring.flyway.default-schema=public",
    ],
)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatusListenerIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @Container
        @ServiceConnection
        val kafka = KafkaContainer("apache/kafka-native:3.8.0")
    }

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    @Autowired
    lateinit var jdbcClient: JdbcClient

    private val objectMapper = ObjectMapper()
    private val producer by lazy {
        KafkaProducer<String, String>(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ),
        )
    }

    @BeforeEach
    fun clean() {
        jdbcClient.sql("truncate contract, capability, contract_mute, heartbeat, sync, sync_page, full_sync_status, event cascade").update()
    }

    @AfterAll
    fun closeProducer() = producer.close()

    @Test
    fun `registration stores the contract with its capabilities, keyed on username and org`() {
        send(StatusTopics.CONTRACT, contract(orgId = "AFK-no", capabilities = setOf(capability("elev"), capability("klasse"))))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals(1, count("select count(*) from contract where username = 'adapter@afk.no' and org_id = 'afk.no'"))
            assertEquals(2, count("select count(*) from capability"))
        }
    }

    @Test
    fun `re-registration replaces the capabilities`() {
        send(StatusTopics.CONTRACT, contract(capabilities = setOf(capability("elev"), capability("klasse"))))
        await.atMost(Duration.ofSeconds(20)) untilAsserted { assertEquals(2, count("select count(*) from capability")) }

        send(StatusTopics.CONTRACT, contract(capabilities = setOf(capability("elev"))))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals(1, count("select count(*) from capability"))
            assertEquals(1, count("select count(*) from contract"))
        }
    }

    @Test
    fun `a tombstone deletes the contract and its capabilities`() {
        send(StatusTopics.CONTRACT, contract(capabilities = setOf(capability("elev"))), key = "adapter@afk.no\u001Fafk.no")
        await.atMost(Duration.ofSeconds(20)) untilAsserted { assertEquals(1, count("select count(*) from capability")) }

        producer.send(ProducerRecord<String, String>(StatusTopics.CONTRACT, "adapter@afk.no\u001Fafk.no", null)).get()

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals(0, count("select count(*) from contract"))
            assertEquals(0, count("select count(*) from capability"))
        }
    }

    @Test
    fun `a contract with event capabilities is stored like any other contract`() {
        send(
            StatusTopics.CONTRACT,
            contract(
                capabilities = setOf(capability("elev")),
                eventCapabilities = setOf(eventCapability("elev", OperationType.READ, OperationType.CREATE)),
            ),
        )

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals(1, count("select count(*) from contract"))
            assertEquals(1, count("select count(*) from capability"))
        }
    }

    @Test
    fun `heartbeats are stored per username and org, whether or not the contract has arrived`() {
        send(StatusTopics.HEARTBEAT, heartbeat(orgId = "ude.oslo.kommune.no"))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals(
                1,
                count("select count(*) from heartbeat where username = 'adapter@afk.no' and org_id = 'ude.oslo.kommune.no'"),
            )
            assertEquals(0, count("select count(*) from contract"))
        }
    }

    @Test
    fun `a sync is completed once every page has arrived, and duplicate pages count once`() {
        val corrId = UUID.randomUUID().toString()
        send(StatusTopics.FULL_SYNC, syncPage(corrId, page = 0, totalPages = 2))
        send(StatusTopics.FULL_SYNC, syncPage(corrId, page = 0, totalPages = 2))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals(1, count("select pages_received from sync where corr_id = '$corrId'"))
        }
        assertNull(completedAt(corrId))

        send(StatusTopics.FULL_SYNC, syncPage(corrId, page = 1, totalPages = 2))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertNotNull(completedAt(corrId))
            assertEquals(200, count("select entities_received from sync where corr_id = '$corrId'"))
            assertEquals("FULL", text("select sync_type from sync where corr_id = '$corrId'"))
            assertEquals("utdanning", text("select domain_name from sync where corr_id = '$corrId'"))
        }
    }

    @Test
    fun `a request without a response is pending, and an answer marks it answered`() {
        val corrId = UUID.randomUUID().toString()
        send(StatusTopics.EVENT_REQUEST, request(corrId))
        await.atMost(Duration.ofSeconds(20)) untilAsserted { assertEquals("PENDING", status(corrId)) }

        send(StatusTopics.EVENT_RESPONSE, response(corrId, adapterId = "adapter-1"))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals("ANSWERED", status(corrId))
            assertEquals("adapter-1", text("select adapter_id from event where corr_id = '$corrId'"))
        }
    }

    @Test
    fun `a READ request and its answer are stored`() {
        val corrId = UUID.randomUUID().toString()
        send(StatusTopics.EVENT_REQUEST, request(corrId, OperationType.READ))
        send(StatusTopics.EVENT_RESPONSE, response(corrId, adapterId = "adapter-1"))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals("ANSWERED", status(corrId))
            assertEquals("READ", text("select operation_type from event where corr_id = '$corrId'"))
        }
    }

    @Test
    fun `an expiry response that arrives before the request still ends up expired with request details`() {
        val corrId = UUID.randomUUID().toString()
        send(StatusTopics.EVENT_RESPONSE, response(corrId, adapterId = null, failed = true, errorMessage = "Event expired."))
        await.atMost(Duration.ofSeconds(20)) untilAsserted { assertEquals("EXPIRED", status(corrId)) }

        send(StatusTopics.EVENT_REQUEST, request(corrId))

        await.atMost(Duration.ofSeconds(20)) untilAsserted {
            assertEquals("EXPIRED", status(corrId))
            assertEquals("elev", text("select resource_name from event where corr_id = '$corrId'"))
        }
    }

    @Test
    fun `an unreadable record is skipped and the listener keeps going`() {
        producer.send(ProducerRecord(StatusTopics.HEARTBEAT, "not json")).get()
        send(StatusTopics.HEARTBEAT, heartbeat())

        await.atMost(Duration.ofSeconds(20)) untilAsserted { assertEquals(1, count("select count(*) from heartbeat")) }
    }

    private fun send(
        topic: String,
        value: Any,
        key: String? = null,
    ) {
        producer.send(ProducerRecord(topic, key, objectMapper.writeValueAsString(value))).get()
    }

    private fun count(sql: String): Long =
        jdbcClient
            .sql(sql)
            .query(Long::class.java)
            .optional()
            .orElse(-1L)

    private fun text(sql: String): String? =
        jdbcClient
            .sql(sql)
            .query(String::class.java)
            .optional()
            .orElse(null)

    private fun status(corrId: String) = text("select status from event where corr_id = '$corrId'")

    private fun completedAt(corrId: String) = text("select completed_at::text from sync where corr_id = '$corrId'")

    private fun contract(
        orgId: String = "afk.no",
        capabilities: Set<AdapterCapability>,
        eventCapabilities: Set<EventCapability> = emptySet(),
    ) = AdapterContract
        .builder()
        .adapterId("adapter-1")
        .orgId(orgId)
        .username("adapter@afk.no")
        .heartbeatIntervalInMinutes(1)
        .capabilities(capabilities)
        .eventCapabilities(eventCapabilities)
        .build()

    private fun capability(resource: String) =
        AdapterCapability
            .builder()
            .domainName("utdanning")
            .packageName("elev")
            .resourceName(resource)
            .fullSyncIntervalInDays(7)
            .deltaSyncInterval(AdapterCapability.DeltaSyncInterval.IMMEDIATE)
            .build()

    private fun eventCapability(
        resource: String,
        vararg operations: OperationType,
    ) = EventCapability
        .builder()
        .domainName("utdanning")
        .packageName("elev")
        .resourceName(resource)
        .operations(operations.toSet())
        .build()

    private fun heartbeat(orgId: String = "afk.no") =
        AdapterHeartbeat
            .builder()
            .adapterId("adapter-1")
            .orgId(orgId)
            .username("adapter@afk.no")
            .time(0)
            .build()

    private fun syncPage(
        corrId: String,
        page: Long,
        totalPages: Long,
    ) = SyncPageMetadata
        .builder()
        .adapterId("adapter-1")
        .corrId(corrId)
        .orgId("afk.no")
        .totalSize(200)
        .page(page)
        .pageSize(100)
        .totalPages(totalPages)
        .uriRef("/utdanning/elev/elev")
        .time(System.currentTimeMillis())
        .build()

    private fun request(
        corrId: String,
        operationType: OperationType = OperationType.CREATE,
    ) = RequestFintEvent
        .builder()
        .corrId(corrId)
        .orgId("afk.no")
        .domainName("utdanning")
        .packageName("elev")
        .resourceName("elev")
        .operationType(operationType)
        .created(System.currentTimeMillis())
        .timeToLive(System.currentTimeMillis() + 900_000)
        .value("{}")
        .build()

    private fun response(
        corrId: String,
        adapterId: String?,
        failed: Boolean = false,
        errorMessage: String? = null,
    ) = ResponseFintEvent
        .builder()
        .corrId(corrId)
        .orgId("afk.no")
        .adapterId(adapterId)
        .handledAt(System.currentTimeMillis())
        .failed(failed)
        .errorMessage(errorMessage)
        .build()
}
