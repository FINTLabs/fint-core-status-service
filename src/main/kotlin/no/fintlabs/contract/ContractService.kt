package no.fintlabs.contract

import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.contract.model.AdapterStatus
import no.fintlabs.contract.model.Contract
import no.fintlabs.contract.model.ContractDto
import no.fintlabs.contract.model.DomainStatus
import no.fintlabs.sync.SyncCacheService
import no.fintlabs.sync.model.SyncMetadata
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.Instant.now

@Service
class ContractService(
    private val contractCache: ContractCache,
    private val syncCacheService: SyncCacheService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun updateActivity(syncMetadata: SyncMetadata) {
        val lastPageTime = syncMetadata.getLastPageTime()

        contractCache.get(syncMetadata.adapterId)?.let { contract ->
            contract.updateLastActivity(lastPageTime)
            if (syncMetadata.syncType == SyncType.FULL
                || syncMetadata.syncType == SyncType.DELTA
                || syncMetadata.syncType == SyncType.DELETE
            ) {
                contract.getCapability(syncMetadata.domain, syncMetadata.`package`, syncMetadata.resource)
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

    fun inactiveContracts(): List<Contract> {
        val aWeekAgo = now().minusMillis(604800000L)
        val inactiveContractsList = mutableListOf<Contract>()
        contractCache.getAll()?.forEach { contract ->
            if (Instant.ofEpochMilli(contract.lastActivity).isBefore(aWeekAgo) && !contract.hasContact) {
                inactiveContractsList.add(contract)
            }
        }
        return inactiveContractsList
    }

    fun getByOrgAndComponent(orgId: String, component: String): MutableList<ContractDto> {
        var contracts = mutableListOf<ContractDto>()
        for (contract in contractCache.getByOrgId(orgId)) {
            if (contract.components.contains(component))
                contracts.add(mapContractDto(contract))
        }
        return contracts
    }

    fun mapContractDto(contract: Contract): ContractDto {
        return ContractDto(
            adapterId = contract.adapterId,
            heartbeat = contract.hasContact,
            lastDelta = syncCacheService.getLastdeltabyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0,
            lastFull = syncCacheService.getLastFyllbyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0
        )
    }

    fun getStatus(): List<AdapterStatus> {
        return contractCache.getAll().map { contract ->
            AdapterStatus(
                organzation = contract.orgId,
                domain = getDomain(contract),
                heartBeat = contract.hasContact
            )
        }
    }

    fun getDomainStatus(orgId: String, domain: String): List<DomainStatus> {
        var domainStatusList = mutableListOf<DomainStatus>()
        getByOrIdAndComponent(orgId, domain).map { contract ->
                domainStatusList.add(DomainStatus(
                    domain = domain,
                    hasContact = contract.hasContact,
                    answersEvents = getFollowsContract(contract, domain),
                    lastDeltaSync = syncCacheService.getLastdeltabyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0,
                    lastFullSync = syncCacheService.getLastFyllbyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0
                )
            )
        }
        return domainStatusList
    }

    private fun getByOrIdAndComponent(orgid: String, component: String): MutableList<Contract> {
        val contracts = mutableListOf<Contract>()
        contractCache.getByOrgId(orgid)?.forEach { contract ->
            val domain = contract.components.any { comp ->
                comp.substringBefore("-") == component
            }
            if (domain) contracts.add(contract)
        }
        return contracts
    }


    private fun getDomain(contract: Contract): String {
        return contract.components.map { component ->
            component.substringBefore("-")
        }.first()
    }

    private fun getFollowsContract(contract: Contract, domain: String): Boolean {
        contract.capabilities.values.forEach { capability ->
            if (capability.componentName == domain)
                return capability.followsContract
        }
        return false
    }
}
