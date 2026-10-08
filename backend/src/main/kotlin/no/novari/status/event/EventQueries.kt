package no.novari.status.event

import no.novari.status.health.instant
import no.novari.status.normalizeOrgId
import no.novari.status.toOffset
import no.novari.status.web.PageRequest
import no.novari.status.web.SqlWhere
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant

/**
 * Events newest first, by when we first saw them. Filtering on an org includes its sub-orgs.
 * answeredBy is the username of the contract with the answering adapter's id on that org.
 */
@Repository
class EventQueries(
    private val jdbcClient: JdbcClient,
) {
    fun find(
        filter: EventFilter,
        page: PageRequest,
    ): Pair<List<EventRow>, Long> {
        val where = where(filter)
        val total = count(filter)
        val rows =
            jdbcClient
                .sql("$SELECT ${where.sql()} order by e.received_at desc, e.corr_id desc limit :limit offset :offset")
                .params(where.params + mapOf("limit" to page.size, "offset" to page.offset))
                .query { rs, _ -> rs.toRow() }
                .list()
        return rows to total
    }

    fun count(filter: EventFilter): Long {
        val where = where(filter)
        return jdbcClient
            .sql("select count(*) from event e ${where.sql()}")
            .params(where.params)
            .query(Long::class.java)
            .single()
    }

    fun get(corrId: String): EventRow? =
        jdbcClient
            .sql("$SELECT where e.corr_id = :corrId")
            .param("corrId", corrId)
            .query { rs, _ -> rs.toRow() }
            .optional()
            .orElse(null)

    fun findUnanswered(
        expiredSince: Instant,
        pendingBefore: Instant,
    ): List<EventRow> =
        jdbcClient
            .sql(
                """
                $SELECT
                where (e.status = 'EXPIRED' and e.received_at >= :expiredSince)
                   or (e.status = 'PENDING' and e.created_at < :pendingBefore)
                """,
            ).param("expiredSince", expiredSince.toOffset())
            .param("pendingBefore", pendingBefore.toOffset())
            .query { rs, _ -> rs.toRow() }
            .list()

    private fun where(filter: EventFilter): SqlWhere =
        SqlWhere()
            .add("e.received_at >= :from", "from" to filter.from.toOffset())
            .addIf(filter.to != null, "e.received_at < :to", "to" to filter.to?.toOffset())
            .addIf(filter.receivedAfter != null, "e.received_at > :after", "after" to filter.receivedAfter?.toOffset())
            .addIf(filter.status != null, "e.status in (:statuses)", "statuses" to filter.status?.statuses?.map { it.name })
            .addIf(
                !filter.orgId.isNullOrBlank(),
                "(e.org_id = :org or e.org_id like :subOrgs)",
                "org" to filter.orgId?.let(::normalizeOrgId),
                "subOrgs" to filter.orgId?.let { "%." + normalizeOrgId(it) },
            ).addIf(!filter.corrId.isNullOrBlank(), "e.corr_id like :corrId", "corrId" to "%${filter.corrId?.trim()?.lowercase()}%")
            .addIf(!filter.domainName.isNullOrBlank(), "e.domain_name = :domain", "domain" to filter.domainName?.lowercase())
            .addIf(!filter.packageName.isNullOrBlank(), "e.package_name = :pkg", "pkg" to filter.packageName?.lowercase())
            .addIf(!filter.resourceName.isNullOrBlank(), "e.resource_name = :resource", "resource" to filter.resourceName?.lowercase())

    private fun ResultSet.toRow() =
        EventRow(
            corrId = getString("corr_id"),
            orgId = getString("org_id"),
            domainName = getString("domain_name"),
            packageName = getString("package_name"),
            resourceName = getString("resource_name"),
            operationType = getString("operation_type"),
            createdAt = instant("created_at"),
            expiresAt = instant("expires_at"),
            receivedAt = instant("received_at")!!,
            handledAt = instant("handled_at"),
            status = EventStatus.valueOf(getString("status")),
            reason = getString("reason"),
            adapterId = getString("adapter_id"),
            answeredBy = getString("answered_by"),
        )

    companion object {
        private const val SELECT = """
            select e.*, (select c.username from contract c where c.adapter_id = e.adapter_id and c.org_id = e.org_id
                         order by c.username limit 1) as answered_by
            from event e
        """
    }
}

data class EventRow(
    val corrId: String,
    val orgId: String,
    val domainName: String?,
    val packageName: String?,
    val resourceName: String?,
    val operationType: String?,
    val createdAt: Instant?,
    val expiresAt: Instant?,
    val receivedAt: Instant,
    val handledAt: Instant?,
    val status: EventStatus,
    val reason: String?,
    val adapterId: String?,
    val answeredBy: String?,
) {
    val unanswered: Boolean get() = status == EventStatus.PENDING || status == EventStatus.EXPIRED
}
