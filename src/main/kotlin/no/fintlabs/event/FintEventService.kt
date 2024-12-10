package no.fintlabs.event

import no.fintlabs.adapter.models.event.FintEvent
import org.springframework.stereotype.Service

@Service
class FintEventService(val eventStatusCache: EventStatusCache) {

//    fun lol(event: FintEvent) {
//        eventStatusCache.add(event)
//    }

    fun getAllEvents(): Collection<EventStatus> {
        return eventStatusCache.cache.values.toList()
    }

    fun getEventById(id: String): EventStatus? {
        return eventStatusCache.cache[id];
    }

}