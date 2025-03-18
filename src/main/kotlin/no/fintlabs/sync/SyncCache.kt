package no.fintlabs.sync

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.sync.kafka.PageProducer
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SyncCache(
    private val pageProducer: PageProducer,
    private val syncMetric: SyncMetric
) {

    private val cache: MutableMap<String, ConcurrentHashMap<String, no.fintlabs.sync.model.SyncMetadata>> =
        ConcurrentHashMap()

    fun getAll(): Collection<no.fintlabs.sync.model.SyncMetadata> =
        cache.values.flatMap { it.values }

    fun getByOrgId(orgId: String): Collection<no.fintlabs.sync.model.SyncMetadata> =
        cache.getOrDefault(orgId, ConcurrentHashMap()).values

    fun add(sync: SyncMetadata, syncType: String) {
        requireNotNull(sync.orgId) { "orgId must be set" }
        requireNotNull(sync.corrId) { "corrId must be set" }

        val orgCache = cache.computeIfAbsent(sync.orgId) { ConcurrentHashMap() }
        orgCache.compute(sync.corrId) { _, existing ->
            existing?.apply {
                addPage(sync)
                processFinishedPage(this)
            } ?: run {
                val create = SyncPageMetadata.crea(sync, syncType)
                processFinishedPage(create)
                create
            }
        }
    }

    fun processFinishedPage(page: no.fintlabs.sync.model.SyncMetadata) {
        if (page.finished) {
//            pageProducer.sendPage(page) disabled due to bug
            syncMetric.incrementCompletedSyncs(page.domain, page.`package`, page.resource)
        }
    }

}
