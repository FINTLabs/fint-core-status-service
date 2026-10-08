package no.novari.status.sync

import no.novari.status.kafka.SyncPageMessage
import no.novari.status.normalizeOrgId
import no.novari.status.toOffset
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * One row per sync (corrId) and one row per received page. A page that arrives twice is
 * only counted once. A sync is completed when every page has been received; this is what
 * the gateway received, not what it has stored. The last completed full sync per org and
 * resource is also kept in full_sync_status, which the retention job does not touch.
 */
@Repository
class SyncStore(
    private val jdbcClient: JdbcClient,
) {
    @Transactional
    fun savePage(
        type: SyncType,
        message: SyncPageMessage,
        at: Instant,
    ) {
        val (domain, pkg, resource) = parseUriRef(message.uriRef) ?: return
        val receivedAt = at.toOffset()

        jdbcClient
            .sql(
                """
                insert into sync (corr_id, sync_type, adapter_id, org_id, domain_name, package_name, resource_name,
                                  total_size, total_pages, pages_received, entities_received, started_at, last_page_at)
                values (:corrId, :type, :adapterId, :orgId, :domain, :pkg, :resource, :totalSize, :totalPages, 0, 0, :at, :at)
                on conflict (corr_id) do nothing
                """,
            ).param("corrId", message.corrId)
            .param("type", type.name)
            .param("adapterId", message.adapterId)
            .param("orgId", normalizeOrgId(message.orgId))
            .param("domain", domain)
            .param("pkg", pkg)
            .param("resource", resource)
            .param("totalSize", message.totalSize)
            .param("totalPages", message.totalPages)
            .param("at", receivedAt)
            .update()

        val newPage =
            jdbcClient
                .sql(
                    """
                    insert into sync_page (corr_id, page, page_size, received_at)
                    values (:corrId, :page, :pageSize, :at)
                    on conflict do nothing
                    """,
                ).param("corrId", message.corrId)
                .param("page", message.page)
                .param("pageSize", message.pageSize)
                .param("at", receivedAt)
                .update() == 1

        if (!newPage) return

        val justCompleted =
            jdbcClient
                .sql(
                    """
                    update sync set
                        pages_received = pages_received + 1,
                        entities_received = entities_received + :pageSize,
                        started_at = least(started_at, :at),
                        last_page_at = greatest(last_page_at, :at),
                        completed_at = case
                            when completed_at is null and pages_received + 1 >= greatest(total_pages, 1) then :at
                            else completed_at
                        end
                    where corr_id = :corrId
                    returning coalesce(completed_at = :at, false)
                    """,
                ).param("corrId", message.corrId)
                .param("pageSize", message.pageSize)
                .param("at", receivedAt)
                .query(Boolean::class.java)
                .single()

        if (justCompleted && type == SyncType.FULL) {
            jdbcClient
                .sql(
                    """
                    insert into full_sync_status (org_id, domain_name, package_name, resource_name, last_completed_at)
                    select org_id, domain_name, package_name, resource_name, completed_at from sync where corr_id = :corrId
                    on conflict (org_id, domain_name, package_name, resource_name) do update set
                        last_completed_at = greatest(full_sync_status.last_completed_at, excluded.last_completed_at)
                    """,
                ).param("corrId", message.corrId)
                .update()
        }
    }

    private fun parseUriRef(uriRef: String): Triple<String, String, String>? {
        val parts = uriRef.trim('/').lowercase().split('/')
        return if (parts.size == 3 && parts.none { it.isBlank() }) Triple(parts[0], parts[1], parts[2]) else null
    }
}
