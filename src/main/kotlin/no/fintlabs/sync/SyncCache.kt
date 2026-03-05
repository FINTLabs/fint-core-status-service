package no.fintlabs.sync

import no.fintlabs.sync.model.Page
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

    @Transactional
    fun add(sync: SyncMetadata) {
        val existing = repository.findByCorrId(sync.corrId)

        val sync = if (existing != null) {
            addPage(existing, sync)
            existing
        } else {
            sync.toEntity()
        }
        syncProgressionService.processPageProgression(existing?.toDomain() ?: sync.toDomain())

        repository.save(sync)
    }

    fun addPage(existing: SyncEntity, sync: SyncMetadata) {
        val newPage = sync.pages[0]
        existing.pages = ((existing.pages ?: emptyList()) + newPage) as MutableList<Page>

        existing.pagesAcquired += 1
        existing.entitiesAquired += sync.entitiesAquired
        existing.finished = existing.totalPages == existing.pagesAcquired
    }
}