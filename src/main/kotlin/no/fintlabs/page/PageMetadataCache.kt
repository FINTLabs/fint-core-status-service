package no.fintlabs.page

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.page.model.PageMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class PageMetadataCache {

    private val cache: MutableMap<String, ConcurrentHashMap<String, PageMetadata>> = ConcurrentHashMap()

    fun getAll(): Collection<PageMetadata> =
        cache.values.flatMap { it.values }

    fun getByOrgId(orgId: String): Collection<PageMetadata> =
        cache.getOrDefault(orgId, ConcurrentHashMap()).values

    fun add(pageMetadata: SyncPageMetadata, syncType: SyncType) {
        requireNotNull(pageMetadata.orgId) { "orgId must be set" }
        requireNotNull(pageMetadata.corrId) { "corrId must be set" }

        val orgCache = cache.computeIfAbsent(pageMetadata.orgId) { ConcurrentHashMap() }
        orgCache.compute(pageMetadata.corrId) { _, existing ->
            if (existing == null) {
                PageMetadata.create(pageMetadata, syncType)
            } else {
                existing.addPage(pageMetadata)
                existing
            }
        }
    }

}
