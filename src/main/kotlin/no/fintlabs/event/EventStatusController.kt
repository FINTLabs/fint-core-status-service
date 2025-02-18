package no.fintlabs.event

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/event")
class EventStatusController(val fintEventService: FintEventService) {

    @GetMapping
    fun get(
        @RequestParam(required = false) from: Long?,
        @RequestParam(required = false) to: Long?
    ): Collection<EventStatus>{
        if (from != null || to != null) return fintEventService.getEventsByTime(from, to)
        return fintEventService.getAllEvents()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<EventStatus> =
        fintEventService.getEventById(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
}