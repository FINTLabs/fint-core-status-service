package no.novari.status.health

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class HealthRulesTest {
    private val now = Instant.parse("2026-10-08T12:00:00Z")

    @Test
    fun `a heartbeat within twice the interval is ok`() {
        assertEquals(HeartbeatHealth.OK, HealthRules.heartbeat(now.minus(Duration.ofSeconds(110)), 1, now))
    }

    @Test
    fun `a heartbeat older than twice the interval has stopped`() {
        assertEquals(HeartbeatHealth.STOPPED, HealthRules.heartbeat(now.minus(Duration.ofSeconds(121)), 1, now))
    }

    @Test
    fun `no heartbeat at all is never`() {
        assertEquals(HeartbeatHealth.NEVER, HealthRules.heartbeat(null, 1, now))
    }

    @Test
    fun `a full sync within the interval is ok and older is overdue`() {
        val registered = now.minus(Duration.ofDays(60))
        assertEquals(FullSyncHealth.OK, HealthRules.fullSync(now.minus(Duration.ofDays(6)), 7, registered, now))
        assertEquals(FullSyncHealth.OVERDUE, HealthRules.fullSync(now.minus(Duration.ofDays(8)), 7, registered, now))
    }

    @Test
    fun `a capability without a full sync waits for one interval after the first registration`() {
        assertEquals(FullSyncHealth.WAITING, HealthRules.fullSync(null, 7, now.minus(Duration.ofDays(3)), now))
        assertEquals(FullSyncHealth.NEVER, HealthRules.fullSync(null, 7, now.minus(Duration.ofDays(8)), now))
    }
}
