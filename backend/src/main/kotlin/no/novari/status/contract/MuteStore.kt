package no.novari.status.contract

import no.novari.status.toOffset
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Mutes are kept per (username, orgId) and not on the contract row, so a mute survives
 * re-registrations. A mute with a muted_until in the past no longer counts.
 */
@Repository
class MuteStore(
    private val jdbcClient: JdbcClient,
) {
    fun save(
        username: String,
        orgId: String,
        mutedBy: String,
        reason: String?,
        mutedUntil: Instant?,
        at: Instant,
    ) {
        jdbcClient
            .sql(
                """
                insert into contract_mute (username, org_id, muted_by, reason, muted_at, muted_until)
                values (:username, :orgId, :mutedBy, :reason, :at, :until)
                on conflict (username, org_id) do update set
                    muted_by = excluded.muted_by,
                    reason = excluded.reason,
                    muted_at = excluded.muted_at,
                    muted_until = excluded.muted_until
                """,
            ).param("username", username)
            .param("orgId", orgId)
            .param("mutedBy", mutedBy)
            .param("reason", reason?.trim()?.takeIf { it.isNotEmpty() })
            .param("until", mutedUntil?.toOffset())
            .param("at", at.toOffset())
            .update()
    }

    fun delete(
        username: String,
        orgId: String,
    ) {
        jdbcClient
            .sql("delete from contract_mute where username = :username and org_id = :orgId")
            .param("username", username)
            .param("orgId", orgId)
            .update()
    }
}
