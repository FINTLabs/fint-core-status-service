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

    private val cache: MutableMap<String, ConcurrentHashMap<String, SyncMetadata>> = ConcurrentHashMap()

    fun getAll(): Collection<SyncMetadata> =
        cache.values.flatMap { it.values }

    fun getByOrgId(orgId: String): Collection<SyncMetadata> =
        cache.getOrDefault(orgId, ConcurrentHashMap()).values

    fun add(syncPageMetdata: SyncPageMetadata, syncType: String) {
        requireNotNull(syncPageMetdata.orgId) { "orgId must be set" }
        requireNotNull(syncPageMetdata.corrId) { "corrId must be set" }

        val orgCache = cache.computeIfAbsent(syncPageMetdata.orgId) { ConcurrentHashMap() }
        orgCache.compute(syncPageMetdata.corrId) { _, existing ->
            existing?.apply {
                addPage(syncPageMetdata)
                processFinishedPage(this)
            } ?: run {
                val create = SyncMetadata.create(syncPageMetdata, syncType)
                processFinishedPage(create)
                create
            }
        }
    }

    fun processFinishedPage(page: SyncMetadata) {
        if (page.finished) {
//            pageProducer.sendPage(page) disabled due to bug
            syncMetric.incrementCompletedSyncs(page.domain, page.`package`, page.resource)
        }
    }

}
