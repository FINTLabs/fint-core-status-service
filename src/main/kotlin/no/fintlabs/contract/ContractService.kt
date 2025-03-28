package no.fintlabs.contract

import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.sync.model.SyncMetadata
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContractService(
    private val contractCache: ContractCache
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun updateActivity(syncMetadata: SyncMetadata) {
        val lastPageTime = syncMetadata.getLastPageTime()

        contractCache.get(syncMetadata.adapterId)?.let { contract ->
            contract.updateLastActivity(lastPageTime)
            if (syncMetadata.syncType == SyncType.FULL) {
                contract.getCapability(syncMetadata.domain, syncMetadata.`package`, syncMetadata.resource)
                    ?.updateLastFullSync(lastPageTime)
                    ?: logger.warn(
                        "Capability not found for adapterId: {} with domain: {}, package: {}, resource: {}",
                        syncMetadata.adapterId,
                        syncMetadata.domain,
                        syncMetadata.`package`,
                        syncMetadata.resource
                    )
            }
        } ?: logger.warn("Contract not found when attempting to update activity")
    }

    fun updateActivity(adapterId: String, time: Long) =
        contractCache.get(adapterId)?.apply { updateLastActivity(time) }

}