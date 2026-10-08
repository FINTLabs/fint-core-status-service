package no.novari.status.sync

import no.novari.status.health.instant
import no.novari.status.normalizeOrgId
import no.novari.status.toOffset
import no.novari.status.web.PageRequest
import no.novari.status.web.PageResponse
import no.novari.status.web.SqlWhere
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Clock
import java.time.Duration

/**
 * A sync without all its pages is RUNNING until no page has arrived for [STALL_AFTER], then
 * STALLED. Filtering on an org includes its sub-orgs.
 */
@Repository
class SyncQueries(
    private val jdbcClient: JdbcClient,
    private val clock: Clock,
) {
    fun find(
        filter: SyncFilter,
        page: PageRequest,
    ): PageResponse<SyncSummary> {
        val where = where(filter)
        val total =
            jdbcClient
                .sql("select count(*) from sync ${where.sql()}")
                .params(where.params)
                .query(Long::class.java)
                .single()
        val items =
            jdbcClient
                .sql(
                    "select *, $STATE as state from sync ${where.sql()} order by started_at desc, corr_id desc limit :limit offset :offset",
                ).params(where.params + mapOf("limit" to page.size, "offset" to page.offset))
                .query { rs, _ -> rs.toSummary() }
                .list()
        return PageResponse(items, page.page, page.size, total)
    }

    fun countStartedAfter(filter: SyncFilter): Long {
        val where = where(filter)
        return jdbcClient
            .sql("select count(*) from sync ${where.sql()}")
            .params(where.params)
            .query(Long::class.java)
            .single()
    }

    fun get(corrId: String): SyncDetail? {
        val where = SqlWhere().add("corr_id = :corrId", "corrId" to corrId).add("true", "stallCutoff" to stallCutoff())
        val sync =
            jdbcClient
                .sql("select *, $STATE as state from sync ${where.sql()}")
                .params(where.params)
                .query { rs, _ -> rs.toSummary() }
                .optional()
                .orElse(null) ?: return null
        val pages =
            jdbcClient
                .sql("select page, page_size, received_at from sync_page where corr_id = :corrId order by page")
                .param("corrId", corrId)
                .query { rs, _ -> SyncPageView(rs.getInt("page"), rs.getInt("page_size"), rs.instant("received_at")!!) }
                .list()
        return SyncDetail(sync, pages)
    }

    private fun where(filter: SyncFilter): SqlWhere =
        SqlWhere()
            .add("started_at >= :from", "from" to filter.from.toOffset(), "stallCutoff" to stallCutoff())
            .addIf(filter.to != null, "started_at < :to", "to" to filter.to?.toOffset())
            .addIf(filter.startedAfter != null, "started_at > :after", "after" to filter.startedAfter?.toOffset())
            .addIf(filter.type != null, "sync_type = :type", "type" to filter.type?.name)
            .addIf(filter.state != null, "$STATE = :state", "state" to filter.state?.name)
            .addIf(
                !filter.orgId.isNullOrBlank(),
                "(org_id = :org or org_id like :subOrgs)",
                "org" to filter.orgId?.let(::normalizeOrgId),
                "subOrgs" to filter.orgId?.let { "%." + normalizeOrgId(it) },
            ).addIf(!filter.corrId.isNullOrBlank(), "corr_id like :corrId", "corrId" to "%${filter.corrId?.trim()?.lowercase()}%")
            .addIf(!filter.domainName.isNullOrBlank(), "domain_name = :domain", "domain" to filter.domainName?.lowercase())
            .addIf(!filter.packageName.isNullOrBlank(), "package_name = :pkg", "pkg" to filter.packageName?.lowercase())
            .addIf(!filter.resourceName.isNullOrBlank(), "resource_name = :resource", "resource" to filter.resourceName?.lowercase())

    private fun stallCutoff() = clock.instant().minus(STALL_AFTER).toOffset()

    private fun ResultSet.toSummary() =
        SyncSummary(
            corrId = getString("corr_id"),
            type = SyncType.valueOf(getString("sync_type")),
            orgId = getString("org_id"),
            domainName = getString("domain_name"),
            packageName = getString("package_name"),
            resourceName = getString("resource_name"),
            adapterId = getString("adapter_id"),
            totalPages = getInt("total_pages"),
            pagesReceived = getInt("pages_received"),
            totalSize = getLong("total_size"),
            entitiesReceived = getLong("entities_received"),
            startedAt = instant("started_at")!!,
            lastPageAt = instant("last_page_at")!!,
            completedAt = instant("completed_at"),
            state = SyncState.valueOf(getString("state")),
        )

    companion object {
        val STALL_AFTER: Duration = Duration.ofMinutes(3)
        private const val STATE =
            "(case when completed_at is not null then 'COMPLETED' when last_page_at < :stallCutoff then 'STALLED' else 'RUNNING' end)"
    }
}
