package no.fintlabs.stats

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/stats")
class StatsController(val service: StatsService) {

    @GetMapping
    fun getStats(): Stats {
        return service.getStats()
    }

}