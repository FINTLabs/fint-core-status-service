package no.fintlabs.sync

import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SyncCache(
    private val syncProgressionService: SyncProgressionService
) {

    private val cache: MutableMap<String, ConcurrentHashMap<String, SyncMetadata>> = ConcurrentHashMap()

    fun getAll(): Collection<SyncMetadata> =
        cache.values.flatMap { it.values }

    fun getByOrgId(orgId: String): Collection<SyncMetadata> =
        cache.getOrDefault(orgId, ConcurrentHashMap()).values

    fun add(sync: SyncMetadata) =
        cache.getOrPut(sync.orgId) { ConcurrentHashMap() }
            .compute(sync.corrId) { _, existing ->
                existing?.apply {
                    addPage(sync)
                    syncProgressionService.processPageProgression(this)
                } ?: run {
                    syncProgressionService.processPageProgression(sync)
                    sync
                }
            }

}
