package no.novari.status.event

import no.novari.status.kafka.RequestEventMessage
import no.novari.status.kafka.ResponseEventMessage
import no.novari.status.normalizeOrgId
import no.novari.status.toOffset
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Requests and responses come from two topics with different producers, so either can
 * arrive first. Both write to the same row, keyed by corrId, and neither overwrites what
 * the other one set.
 */
@Repository
class EventStore(
    private val jdbcClient: JdbcClient,
) {
    fun saveRequest(
        message: RequestEventMessage,
        at: Instant,
    ) {
        jdbcClient
            .sql(
                """
                insert into event (corr_id, org_id, domain_name, package_name, resource_name, operation_type,
                                   created_at, expires_at, status, received_at)
                values (:corrId, :orgId, :domain, :pkg, :resource, :operation, :createdAt, :expiresAt, 'PENDING', :at)
                on conflict (corr_id) do update set
                    domain_name = excluded.domain_name,
                    package_name = excluded.package_name,
                    resource_name = excluded.resource_name,
                    operation_type = excluded.operation_type,
                    created_at = excluded.created_at,
                    expires_at = excluded.expires_at
                """,
            ).param("corrId", message.corrId)
            .param("orgId", normalizeOrgId(message.orgId))
            .param("domain", message.domainName?.lowercase())
            .param("pkg", message.packageName?.lowercase())
            .param("resource", message.resourceName?.lowercase())
            .param("operation", message.operationType)
            .param("createdAt", message.created?.let { Instant.ofEpochMilli(it).toOffset() })
            .param("expiresAt", message.timeToLive?.let { Instant.ofEpochMilli(it).toOffset() })
            .param("at", at.toOffset())
            .update()
    }

    fun saveResponse(
        message: ResponseEventMessage,
        at: Instant,
    ) {
        val (status, reason) = outcomeOf(message)
        jdbcClient
            .sql(
                """
                insert into event (corr_id, org_id, status, adapter_id, handled_at, reason, received_at)
                values (:corrId, :orgId, :status, :adapterId, :handledAt, :reason, :at)
                on conflict (corr_id) do update set
                    status = excluded.status,
                    adapter_id = excluded.adapter_id,
                    handled_at = excluded.handled_at,
                    reason = excluded.reason
                """,
            ).param("corrId", message.corrId)
            .param("orgId", normalizeOrgId(message.orgId))
            .param("status", status.name)
            .param("adapterId", message.adapterId?.takeIf { it.isNotBlank() })
            .param("handledAt", (message.handledAt?.let { Instant.ofEpochMilli(it) } ?: at).toOffset())
            .param("reason", reason)
            .param("at", at.toOffset())
            .update()
    }

    private fun outcomeOf(message: ResponseEventMessage): Pair<EventStatus, String?> =
        when {
            message.failed && message.adapterId.isNullOrBlank() && message.errorMessage == EXPIRED_MESSAGE -> {
                EventStatus.EXPIRED to null
            }

            message.failed -> {
                EventStatus.FAILED to message.errorMessage
            }

            message.rejected -> {
                EventStatus.REJECTED to message.rejectReason
            }

            message.conflicted -> {
                EventStatus.CONFLICTED to message.conflictReason
            }

            else -> {
                EventStatus.ANSWERED to null
            }
        }

    companion object {
        private const val EXPIRED_MESSAGE = "Event expired."
    }
}
