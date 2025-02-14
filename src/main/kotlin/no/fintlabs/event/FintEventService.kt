package no.fintlabs.event

import no.fintlabs.event.cache.EventStatusCache
import org.springframework.stereotype.Service

@Service
class FintEventService(val eventStatusCache: EventStatusCache) {

    fun getAllEvents(): Collection<EventStatus> {
        return eventStatusCache.cache.values.toList()
    }

    fun getEventById(id: String): EventStatus? {
        return eventStatusCache.cache[id]
    }

    fun getEventsByTime(from: Long, to: Long): Collection<EventStatus> {
        return eventStatusCache.cache.values.filter { it.requestEvent?.created in from..to }
    }

}