package no.fintlabs.sync

import no.fintlabs.sync.model.SyncEntity
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SyncCache(
    private val syncProgressionService: SyncProgressionService,
    private val repository: SyncJpaRepository
) {

    @Transactional(readOnly = true)
    fun getAll(): Collection<SyncMetadata> =
        repository.findAll().map { it.toDomain() }

    fun getByOrgId(orgId: String): Collection<SyncMetadata> =
        repository.findByOrgId(orgId).map { it.toDomain() }

    fun add(syncMetadata: SyncMetadata){
        var syncEntity = repository.findByCorrId(syncMetadata.corrId)
        if (syncEntity != null) {
            syncEntity.addPage(syncMetadata.toEntity())
            syncMetadata.addPage(syncMetadata)
            syncProgressionService.processPageProgression(syncMetadata)
        } else {
            repository.save(syncMetadata.toEntity())
            syncProgressionService.processPageProgression(syncMetadata)
        }
    }
}
