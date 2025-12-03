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
    fun getAdapters(): List<AdapterStatusview> = adapterStatusCache.getAdapterStatus()

    @GetMapping("/{orgId}/{component}")
    fun getAdaptersForComponents(
        @PathVariable orgId: String,
        @PathVariable component: String,
    ) = componentOverWiev.getByOrgAndComponent(orgId, component)


    //Skal kunne søke etter komponent og Adapterid
    @GetMapping("/details")
    fun getComponentDetails(
    ) = componentDetailsCache.getComponentDetails()

}