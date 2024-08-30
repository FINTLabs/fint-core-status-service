package no.fintlabs.event

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class FintEventController(val fintEventService: FintEventService) {

    @GetMapping
    fun get(): Collection<FintEvent> {
        return fintEventService.getAllEvents()
    }

}