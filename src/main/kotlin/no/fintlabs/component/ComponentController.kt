package no.fintlabs.component

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/component")
class ComponentController(
    private val componentDetailsCache: ComponentDetailsCache,
) {

    @GetMapping("{orgId}/details")
    fun getComponentDetails(
        @PathVariable orgId: String,
    ) = componentDetailsCache.getComponentDetails(orgId)

}