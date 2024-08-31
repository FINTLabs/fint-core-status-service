package no.fintlabs.page

import no.fintlabs.page.model.PageMetaData
import org.springframework.stereotype.Service

@Service
class PageMetadataService(
    val pageMetadataCache: PageMetadataCache
) {

    fun getAll(): Collection<PageMetaData> = pageMetadataCache.getAll()

    fun getAllByOrg(orgId: String): Collection<PageMetaData> = pageMetadataCache.getAllByOrg(orgId)

}