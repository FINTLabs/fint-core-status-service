package no.fintlabs.sync

import no.fintlabs.sync.kafka.CompletedFullSyncProducer
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SyncCache(
    private val completedFullSyncProducer: CompletedFullSyncProducer,
    private val syncMetric: SyncMetric
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
                    processPageProgression(this)
                } ?: run {
                    processPageProgression(sync)
                    sync
                }
            }


    fun processPageProgression(sync: SyncMetadata) {
        if (sync.finished) {
            completedFullSyncProducer.publishCompletedFullSync(sync)
            syncMetric.incrementCompletedSyncs(sync)
        }
    }

}
