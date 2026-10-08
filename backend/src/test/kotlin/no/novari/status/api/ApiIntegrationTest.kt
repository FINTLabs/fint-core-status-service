package no.novari.status.api

import io.micrometer.core.instrument.MeterRegistry
import no.novari.status.contract.ContractStore
import no.novari.status.event.EventStore
import no.novari.status.heartbeat.HeartbeatStore
import no.novari.status.kafka.CapabilityMessage
import no.novari.status.kafka.ContractMessage
import no.novari.status.kafka.EventCapabilityMessage
import no.novari.status.kafka.HeartbeatMessage
import no.novari.status.kafka.RequestEventMessage
import no.novari.status.kafka.ResponseEventMessage
import no.novari.status.kafka.SyncPageMessage
import no.novari.status.metrics.HealthMetrics
import no.novari.status.sync.SyncStore
import no.novari.status.sync.SyncType
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

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
class ApiIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var jdbcClient: JdbcClient

    @Autowired lateinit var contractStore: ContractStore

    @Autowired lateinit var heartbeatStore: HeartbeatStore

    @Autowired lateinit var syncStore: SyncStore

    @Autowired lateinit var eventStore: EventStore

    @Autowired lateinit var healthMetrics: HealthMetrics

    @Autowired lateinit var meterRegistry: MeterRegistry

    private val now = Instant.now()
    private val token = jwt().jwt { it.subject("henrik@novari.no") }

    @BeforeEach
    fun seed() {
        jdbcClient
            .sql("truncate contract, capability, contract_mute, heartbeat, sync, sync_page, full_sync_status, event cascade")
            .update()

        register("inschool@afk.no", "afk.no", "inschool-afk", now.minus(Duration.ofDays(30)), cap("utdanning", "elev", "elev", 7))
        heartbeatStore.save(HeartbeatMessage("inschool-afk", "afk.no", "inschool@afk.no"), now.minusSeconds(30))
        fullSync("afk-elev", "afk.no", "/utdanning/elev/elev", now.minus(Duration.ofDays(1)))

        register("hrm@afk.no", "afk.no", "hrm-afk", now.minus(Duration.ofDays(30)), cap("administrasjon", "personal", "personalressurs", 1))
        heartbeatStore.save(HeartbeatMessage("hrm-afk", "afk.no", "hrm@afk.no"), now.minus(Duration.ofHours(2)))
        fullSync("afk-hrm", "afk.no", "/administrasjon/personal/personalressurs", now.minus(Duration.ofHours(3)))

        register("arkiv@oslo.kommune.no", "oslo.kommune.no", "arkiv-oslo", now.minus(Duration.ofDays(30)), cap("arkiv", "noark", "sak", 7))
        heartbeatStore.save(HeartbeatMessage("arkiv-oslo", "oslo.kommune.no", "arkiv@oslo.kommune.no"), now.minusSeconds(10))
        fullSync("oslo-sak-old", "oslo.kommune.no", "/arkiv/noark/sak", now.minus(Duration.ofDays(9)))

        register(
            "arkiv@oslo.kommune.no",
            "ude.oslo.kommune.no",
            "arkiv-oslo",
            now.minus(Duration.ofDays(30)),
            cap("arkiv", "noark", "sak", 7),
        )

        register("test@novari.no", "novari.no", "test", now.minus(Duration.ofDays(30)), cap("okonomi", "faktura", "faktura", 7))
        heartbeatStore.save(HeartbeatMessage("test", "novari.no", "test@novari.no"), now.minus(Duration.ofDays(2)))

        eventStore.saveRequest(
            request("ev-expired", "ude.oslo.kommune.no", "arkiv", "noark", "sak", now.minus(Duration.ofHours(1))),
            now.minus(Duration.ofHours(1)),
        )
        eventStore.saveResponse(
            ResponseEventMessage("ev-expired", "ude.oslo.kommune.no", failed = true, errorMessage = "Event expired."),
            now.minus(Duration.ofMinutes(44)),
        )
        eventStore.saveRequest(
            request("ev-answered", "afk.no", "utdanning", "elev", "elev", now.minus(Duration.ofMinutes(10))),
            now.minus(Duration.ofMinutes(10)),
        )
        eventStore.saveResponse(ResponseEventMessage("ev-answered", "afk.no", adapterId = "inschool-afk"), now.minus(Duration.ofMinutes(9)))
        eventStore.saveRequest(
            request("ev-failed", "afk.no", "utdanning", "elev", "elev", now.minus(Duration.ofMinutes(5))),
            now.minus(Duration.ofMinutes(5)),
        )
        eventStore.saveResponse(
            ResponseEventMessage("ev-failed", "afk.no", adapterId = "inschool-afk", failed = true, errorMessage = "Ukjent elev"),
            now.minus(Duration.ofMinutes(4)),
        )
        eventStore.saveRequest(
            request("ev-pending", "afk.no", "administrasjon", "personal", "personalressurs", now.minus(Duration.ofMinutes(7))),
            now.minus(Duration.ofMinutes(7)),
        )
    }

    @Test
    fun `requests without a token are rejected`() {
        mockMvc.perform(get("/api/v1/dashboard")).andExpect(status().isUnauthorized)
    }

    @Test
    fun `the dashboard sums sub-orgs into their main org and puts orgs with problems first`() {
        register("inschool@agderfk.no", "agderfk.no", "inschool-agder", now.minus(Duration.ofDays(30)), cap("utdanning", "elev", "elev", 7))
        heartbeatStore.save(HeartbeatMessage("inschool-agder", "agderfk.no", "inschool@agderfk.no"), now.minusSeconds(5))
        fullSync("agder-elev", "agderfk.no", "/utdanning/elev/elev", now.minus(Duration.ofDays(2)))

        mockMvc
            .perform(get("/api/v1/dashboard").with(token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.orgs[*].orgId").value(contains("afk.no", "novari.no", "oslo.kommune.no", "agderfk.no")))
            .andExpect(jsonPath("$.totals.orgsWithProblems").value(3))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'oslo.kommune.no')].contracts").value(2))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'oslo.kommune.no')].fullSyncOverdue").value(1))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'oslo.kommune.no')].fullSyncNever").value(1))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'oslo.kommune.no')].heartbeatNever").value(1))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'oslo.kommune.no')].eventsExpired").value(1))
            .andExpect(
                jsonPath(
                    "$.orgs[?(@.orgId == 'oslo.kommune.no')].subOrgs[*].orgId",
                ).value(containsInAnyOrder("oslo.kommune.no", "ude.oslo.kommune.no")),
            ).andExpect(jsonPath("$.orgs[?(@.orgId == 'afk.no')].heartbeatStopped").value(1))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'afk.no')].eventsPendingLong").value(1))
    }

    @Test
    fun `muting a contract removes it from the problems and unmuting brings it back`() {
        val id = contractId("test@novari.no", "novari.no")
        mockMvc
            .perform(get("/api/v1/dashboard").with(token))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'novari.no')].heartbeatStopped").value(1))

        mockMvc
            .perform(
                put(
                    "/api/v1/contracts/$id/mute",
                ).with(token).contentType(MediaType.APPLICATION_JSON).content("""{"reason":"testadapter"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.mute.mutedBy").value("henrik@novari.no"))
            .andExpect(jsonPath("$.mute.reason").value("testadapter"))

        mockMvc
            .perform(get("/api/v1/dashboard").with(token))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'novari.no')].heartbeatStopped").value(0))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'novari.no')].muted").value(1))
        mockMvc
            .perform(get("/api/v1/contracts").param("filter", "muted").with(token))
            .andExpect(jsonPath("$.items", hasSize<Any>(1)))
            .andExpect(jsonPath("$.counts.MUTED").value(1))

        mockMvc.perform(delete("/api/v1/contracts/$id/mute").with(token)).andExpect(status().isNoContent)

        mockMvc
            .perform(get("/api/v1/dashboard").with(token))
            .andExpect(jsonPath("$.orgs[?(@.orgId == 'novari.no')].heartbeatStopped").value(1))
    }

    @Test
    fun `metrics report problems, and a muted contract reports healthy`() {
        healthMetrics.refresh()
        val heartbeat = {
            meterRegistry
                .get("fint.status.heartbeat.ok")
                .tag("username", "test@novari.no")
                .gauge()
                .value()
        }
        val fullSync = {
            meterRegistry
                .get(
                    "fint.status.full.sync.ok",
                ).tag("username", "arkiv@oslo.kommune.no")
                .tag("org", "oslo.kommune.no")
                .gauge()
                .value()
        }
        assertEquals(0.0, heartbeat())
        assertEquals(0.0, fullSync())

        mockMvc.perform(put("/api/v1/contracts/${contractId("test@novari.no", "novari.no")}/mute").with(token)).andExpect(status().isOk)
        healthMetrics.refresh()

        assertEquals(1.0, heartbeat())
        assertEquals(
            1.0,
            meterRegistry
                .get("fint.status.contract.muted")
                .tag("username", "test@novari.no")
                .gauge()
                .value(),
        )
    }

    @Test
    fun `contracts are sorted with problems first and can be searched and filtered`() {
        mockMvc
            .perform(get("/api/v1/contracts").with(token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(5))
            .andExpect(jsonPath("$.items[0].heartbeat").value("STOPPED"))
            .andExpect(jsonPath("$.items[4].heartbeat").value("OK"))
            .andExpect(jsonPath("$.counts.HEARTBEAT_STOPPED").value(2))
            .andExpect(jsonPath("$.counts.FULL_SYNC_NEVER").value(2))

        mockMvc
            .perform(get("/api/v1/contracts").param("q", "oslo").param("sort", "org").with(token))
            .andExpect(jsonPath("$.items[*].orgId").value(containsInAnyOrder("oslo.kommune.no", "ude.oslo.kommune.no")))
            .andExpect(jsonPath("$.items[0].orgId").value("oslo.kommune.no"))

        mockMvc
            .perform(get("/api/v1/contracts").param("filter", "full-sync-overdue").with(token))
            .andExpect(jsonPath("$.items[0].username").value("arkiv@oslo.kommune.no"))
            .andExpect(jsonPath("$.items", hasSize<Any>(1)))
    }

    @Test
    fun `a contract shows its capabilities with their full sync health`() {
        val id = contractId("arkiv@oslo.kommune.no", "oslo.kommune.no")
        mockMvc
            .perform(get("/api/v1/contracts/$id").with(token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.adapterId").value("arkiv-oslo"))
            .andExpect(jsonPath("$.capabilities[0].fullSync").value("OVERDUE"))
            .andExpect(jsonPath("$.mainOrgId").value("oslo.kommune.no"))
    }

    @Test
    fun `a contract shows the resources it answers events for, with READ first`() {
        contractStore.saveRegistration(
            ContractMessage(
                "live-afk",
                "afk.no",
                "live@afk.no",
                1,
                setOf(cap("utdanning", "elev", "elev", 7)),
                setOf(EventCapabilityMessage("Utdanning", "Elev", "Elev", setOf("update", "READ", "CREATE"))),
            ),
            now,
        )

        mockMvc
            .perform(get("/api/v1/contracts/${contractId("live@afk.no", "afk.no")}").with(token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.eventCapabilities", hasSize<Any>(1)))
            .andExpect(jsonPath("$.eventCapabilities[0].resourceName").value("elev"))
            .andExpect(jsonPath("$.eventCapabilities[0].operations", contains("READ", "CREATE", "UPDATE")))
    }

    @Test
    fun `syncs default to full syncs from the last seven days and an org includes its sub-orgs`() {
        mockMvc
            .perform(get("/api/v1/syncs").with(token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.items[0].corrId").value("afk-hrm"))
            .andExpect(jsonPath("$.items[0].state").value("COMPLETED"))

        mockMvc
            .perform(
                get("/api/v1/syncs").param("from", now.minus(Duration.ofDays(30)).toString()).param("org", "oslo.kommune.no").with(token),
            ).andExpect(jsonPath("$.items[*].corrId").value(containsInAnyOrder("oslo-sak-old")))

        mockMvc
            .perform(get("/api/v1/syncs/afk-elev").with(token))
            .andExpect(jsonPath("$.pages", hasSize<Any>(2)))
            .andExpect(jsonPath("$.sync.entitiesReceived").value(200))
    }

    @Test
    fun `a sync that stops getting pages is stalled`() {
        syncStore.savePage(
            SyncType.FULL,
            SyncPageMessage("stuck", "inschool-afk", "afk.no", 300, 0, 100, 3, "/utdanning/elev/elev"),
            now.minus(Duration.ofMinutes(10)),
        )

        mockMvc
            .perform(get("/api/v1/syncs").param("state", "stalled").with(token))
            .andExpect(jsonPath("$.items[*].corrId").value(containsInAnyOrder("stuck")))
    }

    @Test
    fun `events are newest first, show who answered and who could have answered`() {
        mockMvc
            .perform(get("/api/v1/events").with(token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(4))
            .andExpect(jsonPath("$.items[0].corrId").value("ev-failed"))
            .andExpect(jsonPath("$.items[0].answeredBy").value("inschool@afk.no"))
            .andExpect(jsonPath("$.items[3].corrId").value("ev-expired"))
            .andExpect(jsonPath("$.items[3].status").value("EXPIRED"))
            .andExpect(jsonPath("$.items[3].candidates[0]").value("arkiv@oslo.kommune.no"))

        mockMvc
            .perform(get("/api/v1/events").param("status", "errors").with(token))
            .andExpect(jsonPath("$.items[*].corrId").value(containsInAnyOrder("ev-failed")))

        mockMvc
            .perform(get("/api/v1/events").param("org", "oslo.kommune.no").with(token))
            .andExpect(jsonPath("$.items[*].corrId").value(containsInAnyOrder("ev-expired")))

        mockMvc
            .perform(get("/api/v1/events/ev-expired").with(token))
            .andExpect(jsonPath("$.candidates[0].username").value("arkiv@oslo.kommune.no"))
            .andExpect(jsonPath("$.candidates[0].heartbeat").value("NEVER"))
    }

    @Test
    fun `the count endpoints report what arrived after a point in time`() {
        val since = now.minus(Duration.ofMinutes(8))
        mockMvc
            .perform(get("/api/v1/events/count").param("since", since.toString()).with(token))
            .andExpect(jsonPath("$.count").value(2))
        mockMvc
            .perform(get("/api/v1/syncs/count").param("since", now.minus(Duration.ofHours(4)).toString()).with(token))
            .andExpect(jsonPath("$.count").value(1))
    }

    @Test
    fun `the model and orgs feed the dropdowns`() {
        mockMvc
            .perform(get("/api/v1/model").with(token))
            .andExpect(
                jsonPath(
                    "$[?(@.name == 'utdanning')].packages[?(@.name == 'elev')].resources[*]",
                ).value(hasItem("elev")),
            )
        mockMvc
            .perform(get("/api/v1/orgs").with(token))
            .andExpect(jsonPath("$[?(@.orgId == 'oslo.kommune.no')].subOrgs[0]").value("ude.oslo.kommune.no"))
    }

    @Test
    fun `info says the environment is enabled by default`() {
        mockMvc.perform(get("/api/v1/info").with(token)).andExpect(jsonPath("$.enabled").value(true))
    }

    @Test
    fun `an unknown filter value is a 400 problem`() {
        mockMvc
            .perform(get("/api/v1/contracts").param("filter", "nope").with(token))
            .andExpect(status().isBadRequest)
            .andExpect(
                content().contentTypeCompatibleWith(
                    MediaType.APPLICATION_PROBLEM_JSON,
                ),
            )
    }

    private fun register(
        username: String,
        orgId: String,
        adapterId: String,
        at: Instant,
        vararg capabilities: CapabilityMessage,
    ) = contractStore.saveRegistration(ContractMessage(adapterId, orgId, username, 1, capabilities.toSet()), at)

    private fun cap(
        domain: String,
        pkg: String,
        resource: String,
        days: Int,
    ) = CapabilityMessage(domain, pkg, resource, days, "IMMEDIATE")

    private fun fullSync(
        corrId: String,
        orgId: String,
        uriRef: String,
        at: Instant,
    ) {
        syncStore.savePage(SyncType.FULL, SyncPageMessage(corrId, "a", orgId, 200, 0, 100, 2, uriRef), at)
        syncStore.savePage(SyncType.FULL, SyncPageMessage(corrId, "a", orgId, 200, 1, 100, 2, uriRef), at.plusSeconds(5))
    }

    private fun request(
        corrId: String,
        orgId: String,
        domain: String,
        pkg: String,
        resource: String,
        created: Instant,
    ) = RequestEventMessage(
        corrId,
        orgId,
        domain,
        pkg,
        resource,
        "CREATE",
        created.toEpochMilli(),
        created.plus(Duration.ofMinutes(15)).toEpochMilli(),
    )

    private fun contractId(
        username: String,
        orgId: String,
    ): Long =
        jdbcClient
            .sql("select id from contract where username = :u and org_id = :o")
            .param("u", username)
            .param("o", orgId)
            .query(Long::class.java)
            .single()
}
