package no.fintlabs.page

import no.fintlabs.page.model.PageMetadata
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class PageMetadataService(
    val pageMetadataCache: PageMetadataCache
) {

    fun getAll(): Collection<PageMetadata> =
        pageMetadataCache.cache.values.flatMap { it.values }

    fun getAllByOrg(orgId: String): Collection<PageMetadata> =
        pageMetadataCache.cache.getOrDefault(orgId, ConcurrentHashMap()).values

}