package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterHeartbeat
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.contract.model.*
import no.fintlabs.sync.SyncService
import no.fintlabs.sync.model.SyncMetadata
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
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

    fun getByOrgAndComponent(orgId: String, component: String): MutableSet<ComponentStatus> {
        val contracts = contractJpaRepository.findByOrgIdAndCapabilitiesComponentName(orgId, component)
        val syncsByAdapter = syncService.getByAdapterIds(contracts.map { it.adapterId }.toSet())
        return contracts.map { contract ->
            val syncs = syncsByAdapter[contract.adapterId] ?: emptyList()
            ComponentStatus(
                adapterId = contract.adapterId,
                heartbeat = contract.hasContact,
                lastDelta = syncs.firstOrNull { it.syncType == SyncType.DELTA }?.getLastPageTime() ?: 0,
                lastFull = syncs.firstOrNull { it.syncType == SyncType.FULL }?.getLastPageTime() ?: 0
            )
        }.toMutableSet()
    }

    fun getStatus(): Set<AdapterOverview> {
        return contractJpaRepository.findAll()
            .groupBy { contract -> contract.orgId to getDomain(contract) }
            .map { (key, contracts) ->
                val (orgId, domain) = key
                val statuses = contracts.map { findHealthStatus(it) }
                AdapterOverview(
                    organzation = orgId,
                    domain = domain,
                    status = statuses.firstOrNull { it == AdapterStatusEnum.HEALTHY }
                        ?: statuses.first()
                )
            }.toSet()
    }

    fun getDomainForOrg(orgId: String, domain: String): Set<DomainStatus> {
        val contracts = contractJpaRepository.findByOrgIdAndDomain(orgId, domain)
        val syncsByAdapter = syncService.getByAdapterIds(contracts.map { it.adapterId }.toSet())
        return contracts.map { contract ->
            val syncs = syncsByAdapter[contract.adapterId] ?: emptyList()
            DomainStatus(
                component = getComponent(domain, contract),
                hasContact = contract.hasContact,
                answersEvents = getFollowsContractForDomain(contract, domain),
                lastDeltaSync = syncs.firstOrNull { it.syncType == SyncType.DELTA }?.getLastPageTime() ?: 0,
                lastFullSync = syncs.firstOrNull { it.syncType == SyncType.FULL }?.getLastPageTime() ?: 0
            )
        }.toSet()
    }

    private fun getComponent(domain: String, contract: ContractEntity): String {
        contract.getComponents().map { component ->
            if (component.contains(domain)) return component
        }
        return ""
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

    private fun findHealthStatus(contract: ContractEntity): AdapterStatusEnum =
        when {
            contract.hasContact && getFollowsContract(contract) ->
                AdapterStatusEnum.HEALTHY

            !contract.hasContact ->
                AdapterStatusEnum.NO_HEARTBEAT

            !getFollowsContract(contract) ->
                AdapterStatusEnum.NOT_FOLLOWING_CONTRACT

            else ->
                AdapterStatusEnum.NO_STATUS
        }

    fun updateHeartbeat(value: AdapterHeartbeat) {
        contractJpaRepository.findByIdOrNull(value.adapterId)?.let { contract ->
            contract.lastHeartbeat = value.time
            contract.hasContact = true
            contract.updateLastActivity(value.time)
            contractJpaRepository.save(contract)
        } ?: logger.warn("Received heartbeat for unknown adapterId: {}", value.adapterId)
    }
}