package no.fintlabs.adapterdata

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/component")
class ComponentController(
    private val adapterDataCache: AdapterDataCache
) {

    @GetMapping
    fun getComponents(): ResponseEntity<MutableMap<String, MutableList<ComponentData>>> {
        return ResponseEntity.ok(adapterDataCache.getAll())
    }

}