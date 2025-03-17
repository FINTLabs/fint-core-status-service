package no.fintlabs.page

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.contract.ContractCache
import no.fintlabs.page.kafka.PageProducer
import no.fintlabs.page.model.PageMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class PageMetadataCache(
    private val pageProducer: PageProducer
) {

    private val cache: MutableMap<String, ConcurrentHashMap<String, PageMetadata>> = ConcurrentHashMap()


    fun add(pageMetadata: SyncPageMetadata, syncType: String) {
        requireNotNull(pageMetadata.orgId) { "orgId must be set" }
        requireNotNull(pageMetadata.corrId) { "corrId must be set" }

        val orgCache = cache.computeIfAbsent(pageMetadata.orgId) { ConcurrentHashMap() }
        orgCache.compute(pageMetadata.corrId) { _, existing ->
            existing?.apply {
                addPage(pageMetadata)
                if (finished){
                    pageProducer.sendPage(this)
                }
            } ?: run {
                    val create = PageMetadata.create(pageMetadata, syncType)
                    if (create.finished){
                        pageProducer.sendPage(create)
                    }
                    create
                }
        }
    }



}
