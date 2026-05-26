package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterHeartbeat
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.contract.model.*
import no.fintlabs.sync.SyncService
import no.fintlabs.sync.model.SyncMetadata
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContractService(
    private val syncService: SyncService,
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

    fun getByOrgAndComponent(orgId: String, component: String): MutableSet<ContractDto> {
        return contractJpaRepository.findByOrgIdAndCapabilitiesComponentName(orgId, component)
            .map { mapContractDto(it) }
            .toMutableSet()
    }

    fun mapContractDto(contract: ContractEntity): ContractDto {
        return ContractDto(
            adapterId = contract.adapterId,
            heartbeat = contract.hasContact,
            lastDelta = syncService.getLastdeltabyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0,
            lastFull = syncService.getLastFyllbyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0
        )
    }

    fun getStatus(): Set<AdapterStatus> {
        return contractJpaRepository.findAll().map { contract ->
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
                    lastDeltaSync = syncService.getLastdeltabyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0,
                    lastFullSync = syncService.getLastFyllbyAdapterId(contract.adapterId)?.getLastPageTime() ?: 0
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


    private fun getDomain(contract: ContractEntity): String {
        return contract.getComponents().map { component ->
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

    private fun getFollowsContract(contract: ContractEntity): Boolean {
        return contract.capabilities.none { !it.followsContract }
    }

    private fun calculateHealth(contract: ContractEntity): AdapterStatusEnum =
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