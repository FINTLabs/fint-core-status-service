package no.novari.status.health

import no.novari.status.org.OrgTree
import no.novari.status.toOffset
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime

/**
 * Loads every contract with its capabilities, event capabilities, last heartbeat, last full syncs and mute, and
 * works out their health. There are a few hundred contracts at most, so the whole set is
 * read on each request instead of filtering in SQL.
 */
@Service
class ContractSnapshotService(
    private val jdbcClient: JdbcClient,
    private val clock: Clock,
) {
    fun snapshot(): ContractSnapshot {
        val now = clock.instant()
        val capabilityRows = loadCapabilities()
        val eventCapabilities = loadEventCapabilities()
        val rows =
            jdbcClient
                .sql(
                    """
                    select c.id, c.username, c.org_id, c.adapter_id, c.heartbeat_interval_min,
                           c.first_registered_at, c.registered_at, h.last_seen_at,
                           m.muted_by, m.reason, m.muted_at, m.muted_until
                    from contract c
                    left join heartbeat h on h.username = c.username and h.org_id = c.org_id
                    left join contract_mute m on m.username = c.username and m.org_id = c.org_id
                        and (m.muted_until is null or m.muted_until > :now)
                    """,
                ).param("now", now.toOffset())
                .query { rs, _ -> rs.toContractRow() }
                .list()
        val tree = OrgTree(rows.map { it.orgId })
        val contracts =
            rows.map { row ->
                ContractView(
                    id = row.id,
                    username = row.username,
                    orgId = row.orgId,
                    mainOrgId = tree.mainOf(row.orgId),
                    adapterId = row.adapterId,
                    heartbeatIntervalMinutes = row.heartbeatIntervalMinutes,
                    firstRegisteredAt = row.firstRegisteredAt,
                    registeredAt = row.registeredAt,
                    lastHeartbeatAt = row.lastHeartbeatAt,
                    heartbeat = HealthRules.heartbeat(row.lastHeartbeatAt, row.heartbeatIntervalMinutes, now),
                    capabilities =
                        capabilityRows[row.id].orEmpty().map { cap ->
                            CapabilityView(
                                domainName = cap.domainName,
                                packageName = cap.packageName,
                                resourceName = cap.resourceName,
                                fullSyncIntervalDays = cap.fullSyncIntervalDays,
                                deltaSyncInterval = cap.deltaSyncInterval,
                                lastFullSyncAt = cap.lastCompletedAt,
                                fullSync = HealthRules.fullSync(cap.lastCompletedAt, cap.fullSyncIntervalDays, row.firstRegisteredAt, now),
                            )
                        },
                    eventCapabilities = eventCapabilities[row.id].orEmpty(),
                    mute = row.mute,
                )
            }
        return ContractSnapshot(contracts, now)
    }

    private fun loadCapabilities(): Map<Long, List<CapabilityRow>> =
        jdbcClient
            .sql(
                """
                select cap.contract_id, cap.domain_name, cap.package_name, cap.resource_name,
                       cap.full_sync_interval_days, cap.delta_sync_interval, fs.last_completed_at
                from capability cap
                join contract c on c.id = cap.contract_id
                left join full_sync_status fs
                    on fs.org_id = c.org_id and fs.domain_name = cap.domain_name
                    and fs.package_name = cap.package_name and fs.resource_name = cap.resource_name
                order by cap.domain_name, cap.package_name, cap.resource_name
                """,
            ).query { rs, _ ->
                CapabilityRow(
                    contractId = rs.getLong("contract_id"),
                    domainName = rs.getString("domain_name"),
                    packageName = rs.getString("package_name"),
                    resourceName = rs.getString("resource_name"),
                    fullSyncIntervalDays = rs.getInt("full_sync_interval_days"),
                    deltaSyncInterval = rs.getString("delta_sync_interval"),
                    lastCompletedAt = rs.instant("last_completed_at"),
                )
            }.list()
            .groupBy { it.contractId }

    private fun loadEventCapabilities(): Map<Long, List<EventCapabilityView>> =
        jdbcClient
            .sql(
                """
                select contract_id, domain_name, package_name, resource_name, operations
                from event_capability
                order by domain_name, package_name, resource_name
                """,
            ).query { rs, _ ->
                rs.getLong("contract_id") to
                    EventCapabilityView(
                        domainName = rs.getString("domain_name"),
                        packageName = rs.getString("package_name"),
                        resourceName = rs.getString("resource_name"),
                        operations = (rs.getArray("operations").array as Array<*>).map { it.toString() }.sortedBy(::operationOrder),
                    )
            }.list()
            .groupBy({ it.first }, { it.second })

    private fun ResultSet.toContractRow() =
        ContractRow(
            id = getLong("id"),
            username = getString("username"),
            orgId = getString("org_id"),
            adapterId = getString("adapter_id"),
            heartbeatIntervalMinutes = getInt("heartbeat_interval_min"),
            firstRegisteredAt = instant("first_registered_at")!!,
            registeredAt = instant("registered_at")!!,
            lastHeartbeatAt = instant("last_seen_at"),
            mute =
                getString("muted_by")?.let {
                    MuteView(it, getString("reason"), instant("muted_at")!!, instant("muted_until"))
                },
        )

    private data class ContractRow(
        val id: Long,
        val username: String,
        val orgId: String,
        val adapterId: String,
        val heartbeatIntervalMinutes: Int,
        val firstRegisteredAt: Instant,
        val registeredAt: Instant,
        val lastHeartbeatAt: Instant?,
        val mute: MuteView?,
    )

    private data class CapabilityRow(
        val contractId: Long,
        val domainName: String,
        val packageName: String,
        val resourceName: String,
        val fullSyncIntervalDays: Int,
        val deltaSyncInterval: String?,
        val lastCompletedAt: Instant?,
    )
}

private val OPERATION_ORDER = listOf("READ", "CREATE", "UPDATE", "DELETE", "VALIDATE")

private fun operationOrder(operation: String): Int = OPERATION_ORDER.indexOf(operation).let { if (it < 0) OPERATION_ORDER.size else it }

fun ResultSet.instant(column: String): Instant? = getObject(column, OffsetDateTime::class.java)?.toInstant()
