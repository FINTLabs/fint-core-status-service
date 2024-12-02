package no.fintlabs.event

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class FintEventController(val fintEventService: FintEventService) {

    @GetMapping
    fun get(): Collection<StatusEvent> = fintEventService.getAllEvents()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<StatusEvent> =
        fintEventService.getEventById(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

}