package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.adapter.models.AdapterHeartbeat
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.contract.model.*
import no.fintlabs.sync.SyncCacheService
import no.fintlabs.sync.model.SyncMetadata
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.Instant.now
import kotlin.contracts.contract

@Service
class ContractService(
    private val contractCache: ContractCache,
    private val syncCacheService: SyncCacheService,
    private val contractJpaRepository: ContractJpaRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun updateActivity(syncMetadata: SyncMetadata) {
        logger.info("Updating activity for {}", syncMetadata.adapterId)
        val lastPageTime = syncMetadata.getLastPageTime()
        val contract = contractJpaRepository.findById(syncMetadata.adapterId)
        if (contract.isPresent) {
            val entity = contract.get()
            entity.updateLastActivity(lastPageTime)
            if (syncMetadata.syncType in setOf(SyncType.FULL, SyncType.DELTA, SyncType.DELETE)) {
                entity.getCapability(syncMetadata.domain, syncMetadata.`package`, syncMetadata.resource)
                    ?.updateLastFullSync(lastPageTime)
                    ?: logger.warn(
                        "Capability not found for adapterId: {} with domain: {}, package: {}, resource: {}",
                        syncMetadata.adapterId,
                        syncMetadata.domain,
                        syncMetadata.`package`,
                        syncMetadata.resource
                    )
            }
            contractJpaRepository.save(entity)
        }
    }

    fun getAll() = contractJpaRepository.findAll()

    fun updateActivity(adapterId: String, time: Long) {
        contractJpaRepository.findById(adapterId).ifPresent { entity ->
            entity.updateLastActivity(time)
            contractJpaRepository.save(entity)
        }
    }

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

    fun getByOrgAndComponent(orgId: String, component: String): MutableSet<ContractDto> {
        var contracts = mutableSetOf<ContractDto>()
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

    fun getStatus(): Set<AdapterStatus> {
        return contractCache.getAll().map { contract ->
            AdapterStatus(
                organzation = contract.orgId,
                domain = getDomain(contract),
                status = calculateHealth(contract)
            )
        }.toSet()
    }

    fun getDomainForOrg(orgId: String, domain: String): Set<DomainStatus> {
        return getByOrIdAndComponent(orgId, domain)
            .map { contract ->
                DomainStatus(
                    component = getComponent(domain, contract),
                    hasContact = contract.hasContact,
                    answersEvents = getFollowsContractForDomain(contract, domain),
                    lastDeltaSync = syncCacheService.getLastdeltabyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0,
                    lastFullSync = syncCacheService.getLastFyllbyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0
                )
            }
            .distinctBy { it.component }
            .toSet()
    }


    private fun getComponent(domain: String, contract: ContractEntity): String {
        contract.getComponents().map { component ->
            if (component.contains(domain)) return component
        }
        return ""
    }

    private fun getByOrIdAndComponent(orgid: String, component: String): MutableList<ContractEntity> {
        val contracts = mutableListOf<ContractEntity>()
        contractJpaRepository.getByOrgId(orgid)?.forEach { contract ->
            val domain = contract.getComponents().any { comp ->
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

    private fun getFollowsContractForDomain(contract: ContractEntity, domain: String): Boolean {
        contract.capabilities.forEach { capability ->
            if (capability.componentName == domain)
                return capability.followsContract
        }
        return false
    }

    private fun getFollowsContract(contract: Contract): Boolean {
        return contract.capabilities.values.none { !it.followsContract }
    }

    private fun calculateHealth(contract: Contract): AdapterStatusEnum =
        when {
            contract.hasContact && getFollowsContract(contract) ->
                AdapterStatusEnum.HEALTHY

            !contract.hasContact ->
                AdapterStatusEnum.NO_HEARTBEAT

            !getFollowsContract(contract) ->
                AdapterStatusEnum.NOT_FOLLOWING_CONTRACT

            else ->
                AdapterStatusEnum.UNOWN_STATUS
        }

    fun updateHeartbeat(value: AdapterHeartbeat) {
        val contract = contractJpaRepository.findById(value.adapterId)
        if (contract.isPresent) {
            contract.get().lastHeartbeat = value.time
            contract.get().hasContact = true
            contract.get().updateLastActivity(value.time)
            contractJpaRepository.save(contract.get())
        }
    }
}