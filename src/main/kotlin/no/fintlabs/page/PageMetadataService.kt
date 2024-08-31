package no.fintlabs.page

import no.fintlabs.page.model.PageMetaData
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class PageMetadataService(
    val pageMetadataCache: PageMetadataCache
) {

    fun getAll(): Collection<PageMetaData> =
        pageMetadataCache.cache.values.flatMap { it.values }

    fun getAllByOrg(orgId: String): Collection<PageMetaData> =
        pageMetadataCache.cache.getOrDefault(orgId, ConcurrentHashMap()).values

}