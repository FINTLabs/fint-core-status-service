package no.fintlabs.contract

import jakarta.annotation.PostConstruct
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.sync.model.SyncMetadata
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.Instant.*

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


    @Scheduled(cron = "0 0 * * * *")
    fun inactiveContract()=
        contractCache.getAll()?.forEach { contract ->
            if (ofEpochMilli(contract.lastActivity).isAfter(getTimeStampFromAWeekAgo()) || !contract.hasContact)
                logger.info("Inactive contract: {}", contract.username)

        }

    private fun getTimeStampFromAWeekAgo() = now().minusMillis(604800000L)
}