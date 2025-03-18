package no.fintlabs.sync

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.sync.kafka.PageProducer
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SyncCache(
    private val pageProducer: PageProducer,
    private val syncMetric: SyncMetric
) {

    private val cache: MutableMap<String, ConcurrentHashMap<String, no.fintlabs.sync.model.SyncMetadata>> = ConcurrentHashMap()

    fun getAll(): Collection<no.fintlabs.sync.model.SyncMetadata> =
        cache.values.flatMap { it.values }

    fun getByOrgId(orgId: String): Collection<no.fintlabs.sync.model.SyncMetadata> =
        cache.getOrDefault(orgId, ConcurrentHashMap()).values

    fun add(syncPageMetadata: no.fintlabs.adapter.models.sync.SyncPageMetadata, syncType: String) {
        requireNotNull(syncPageMetadata.orgId) { "orgId must be set" }
        requireNotNull(syncPageMetadata.corrId) { "corrId must be set" }

        val orgCache = cache.computeIfAbsent(syncPageMetadata.orgId) { ConcurrentHashMap() }
        orgCache.compute(syncPageMetadata.corrId) { _, existing ->
            existing?.apply {
                addPage(syncPageMetadata)
                processFinishedPage(this)
            } ?: run {
                val create = SyncPageMetadata.create(syncPageMetadata, syncType)
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
