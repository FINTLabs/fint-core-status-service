package no.novari.status.contract

import no.novari.status.kafka.ContractMessage
import no.novari.status.normalizeOrgId
import no.novari.status.toOffset
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Contracts are identified by (username, orgId), the same key the adapter gateway uses.
 * A new registration replaces the capability list but keeps the first registration time,
 * since gateways publish all their contracts again when they start. A tombstone on the
 * contract topic deletes the contract, its capabilities and any mute.
 */
@Repository
class ContractStore(
    private val jdbcClient: JdbcClient,
) {
    @Transactional
    fun saveRegistration(
        message: ContractMessage,
        at: Instant,
    ) {
        val contractId =
            jdbcClient
                .sql(
                    """
                    insert into contract (username, org_id, adapter_id, heartbeat_interval_min, first_registered_at, registered_at)
                    values (:username, :orgId, :adapterId, :interval, :at, :at)
                    on conflict (username, org_id) do update set
                        adapter_id = excluded.adapter_id,
                        heartbeat_interval_min = excluded.heartbeat_interval_min,
                        registered_at = excluded.registered_at
                    returning id
                    """,
                ).param("username", message.username)
                .param("orgId", normalizeOrgId(message.orgId))
                .param("adapterId", message.adapterId)
                .param("interval", message.heartbeatIntervalInMinutes)
                .param("at", at.toOffset())
                .query(Long::class.java)
                .single()

        jdbcClient
            .sql("delete from capability where contract_id = :contractId")
            .param("contractId", contractId)
            .update()

        message.capabilities.forEach { capability ->
            jdbcClient
                .sql(
                    """
                    insert into capability (contract_id, domain_name, package_name, resource_name, full_sync_interval_days, delta_sync_interval)
                    values (:contractId, :domain, :pkg, :resource, :fullSyncDays, :deltaSyncInterval)
                    on conflict do nothing
                    """,
                ).param("contractId", contractId)
                .param("domain", capability.domainName.lowercase())
                .param("pkg", capability.packageName.lowercase())
                .param("resource", capability.resourceName.lowercase())
                .param("fullSyncDays", capability.fullSyncIntervalInDays)
                .param("deltaSyncInterval", capability.deltaSyncInterval)
                .update()
        }
    }

    @Transactional
    fun delete(
        username: String,
        orgId: String,
    ) {
        listOf("contract", "contract_mute").forEach { table ->
            jdbcClient
                .sql("delete from $table where username = :username and org_id = :orgId")
                .param("username", username)
                .param("orgId", normalizeOrgId(orgId))
                .update()
        }
    }
}
