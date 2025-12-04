package no.fintlabs.component

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/component")
class ComponentController(
    private val adapterStatusCache: AdapterStatusCache,
    private val componentDetailsCache: ComponentDetailsCache,
    private val componentOverWiev: ComponentOverWievCache,
) {

    @GetMapping
    fun getAdapters(): Set<AdapterStatusview> = adapterStatusCache.getAdapterStatus()

    @GetMapping("/{orgId}/{component}")
    fun getAdaptersForComponents(
        @PathVariable orgId: String,
        @PathVariable component: String,
    ) = componentOverWiev.getByOrgAndComponent(orgId, component)


    @GetMapping("{orgId}/details")
    fun getComponentDetails(
        @PathVariable orgId: String,
    ) = componentDetailsCache.getComponentDetails(orgId)

}