package no.fintlabs.sync

import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.sync.model.CustomerResourceMetadata
import no.fintlabs.sync.model.CustomerResourceSyncStatus
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service
import kotlin.String

@Service
class KundeportalService(
    private val syncCache: SyncCache,
) {

    fun getLastOnDomain(orgId: String): List<CustomerResourceSyncStatus> {
        return syncCache.getByOrgId(orgId)
            .asSequence()
            .filter { it.syncType.isFull() }
            .groupBy {it.toResourceKey() }
            .map { (key, syncs) ->
                val sorted = syncs.sortedByDescending { it.getLastPageTime() }

                val latest = sorted.firstOrNull()
                val previousCompleted = if (latest?.finished == false) {
                    sorted.drop(1).firstOrNull { it.finished }
                } else {
                    null
                }

                CustomerResourceSyncStatus(
                    adapterId = key.adapterId,
                    domain = key.domain,
                    `package` = key.`package`,
                    resource = key.resource,
                    latestFullSync = latest?.toCustomerResourceMetadata(),
                    previousCompletedFullSync = previousCompleted?.toCustomerResourceMetadata()
                )
            }
            .sortedWith(
                compareBy<CustomerResourceSyncStatus> { it.adapterId }
                    .thenBy {it.domain }
                    .thenBy { it.`package` }
                    .thenBy { it.resource }
            )
            .toList()
    }

    private fun SyncMetadata.toResourceKey() = ResourceKey(
        adapterId = adapterId,
        domain = domain,
        `package` = `package`,
        resource = resource,
    )

private fun SyncMetadata.toCustomerResourceMetadata() = CustomerResourceMetadata(
    corrId = corrId,
    adapterId = adapterId,
    domain = domain,
    `package` = `package`,
    resource = resource,
    totalEntries = totalEntities,
    timeOfLastFullSync = getLastPageTime(),
    finished = finished,
)

    private fun SyncType.isFull() = this == SyncType.FULL
}

private data class ResourceKey(
    val adapterId: String,
    val domain: String,
    val `package`: String,
    val resource: String,
)