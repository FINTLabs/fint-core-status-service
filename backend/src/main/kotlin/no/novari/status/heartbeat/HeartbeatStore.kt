package no.novari.status.heartbeat

import no.novari.status.kafka.HeartbeatMessage
import no.novari.status.normalizeOrgId
import no.novari.status.toOffset
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * The last heartbeat per (username, orgId), kept apart from the contract. Heartbeats and
 * contracts come from different topics, so a heartbeat can arrive before its contract; this
 * way neither has to wait for the other. Heartbeats without a contract are not shown.
 */
@Repository
class HeartbeatStore(
    private val jdbcClient: JdbcClient,
) {
    fun save(
        message: HeartbeatMessage,
        at: Instant,
    ) {
        jdbcClient
            .sql(
                """
                insert into heartbeat (username, org_id, adapter_id, last_seen_at)
                values (:username, :orgId, :adapterId, :at)
                on conflict (username, org_id) do update set
                    adapter_id = excluded.adapter_id,
                    last_seen_at = greatest(heartbeat.last_seen_at, excluded.last_seen_at)
                """,
            ).param("username", message.username)
            .param("orgId", normalizeOrgId(message.orgId))
            .param("adapterId", message.adapterId)
            .param("at", at.toOffset())
            .update()
    }
}
