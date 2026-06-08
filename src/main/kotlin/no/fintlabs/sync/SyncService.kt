package no.fintlabs.sync

import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SyncService(
    private val syncProgressionService: SyncProgressionService,
    private val repository: SyncJpaRepository
) {

    @Transactional(readOnly = true)
    fun getAll(): Collection<SyncMetadata> =
        repository.findAll().map { it.toDomain() }

    fun getByOrgId(orgId: String): Collection<SyncMetadata> =
        repository.findByOrgId(orgId).map { it.toDomain() }

    @Transactional
    fun add(incoming: SyncMetadata) {
        val existing = repository.findByCorrId(incoming.corrId)
        val entity = if (existing != null) {
            existing.addPage(incoming)
            existing
        } else {
            incoming.toEntity()
        }
        entity.updateFinished()
        syncProgressionService.processPageProgression(entity.toDomain())
        repository.save(entity)
    }

    fun getByTimeRange(from: Long?, to: Long?): Collection<SyncMetadata> {
        var twentyFourHoursAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        if (from != null && to != null) return repository.findByTime(from, to).map { it.toDomain() }
        else return repository.findByTime(twentyFourHoursAgo, System.currentTimeMillis()).map { it.toDomain() }
    }

    fun getLastFyllbyAdapterId(adapterId: String): SyncMetadata? =
        getAll().firstOrNull { it.adapterId == adapterId && it.syncType.isFull() }

    fun getLastdeltabyAdapterId(adapterId: String): SyncMetadata? =
        getAll().firstOrNull { it.adapterId == adapterId && it.syncType.isDelta() }

    fun getByAdapterIds(adapterIds: Set<String>): Map<String, List<SyncMetadata>> =
        repository.findByAdapterIdIn(adapterIds)
            .map { it.toDomain() }
            .groupBy { it.adapterId }

    fun getByCorrId(id: String): SyncMetadata? = getAll().firstOrNull { it.corrId == id }

    private fun SyncType.isFull() = this == SyncType.FULL

    private fun SyncType.isDelta() = this == SyncType.DELTA
}
