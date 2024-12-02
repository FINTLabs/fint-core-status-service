package no.fintlabs.event

import jakarta.annotation.PostConstruct
import no.fintlabs.adapter.models.event.RequestFintEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class EventStatusController(val fintEventService: FintEventService) {

    @PostConstruct
    fun init() {
        fintEventService.lol(RequestFintEvent.builder().corrId("hotgut69(sexNummer)").orgId("SEGS.COM.REAL").build())
    }

    @GetMapping
    fun get(): Collection<EventStatus> = fintEventService.getAllEvents()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<EventStatus> =
        fintEventService.getEventById(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()

}