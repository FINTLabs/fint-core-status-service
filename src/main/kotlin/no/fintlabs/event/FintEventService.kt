package no.fintlabs.event

import no.fintlabs.event.cache.EventStatusCache
import org.springframework.stereotype.Service
import java.util.*

@Service
class FintEventService(val eventStatusCache: EventStatusCache) {

    fun getAllEvents(): Collection<EventStatus> {
        return eventStatusCache.cache.values.toList()
    }

    fun getEventById(id: String): EventStatus? {
        return eventStatusCache.cache[id]
    }

    fun getEventsByTime(from: Long?, to: Long?): Collection<EventStatus> {
        val current = Date().time
        return when {
            from != null && to != null -> {
                eventStatusCache.cache.values.filter { it.requestEvent?.created in from..to }
            }
            from != null -> {
                eventStatusCache.cache.values.filter { it.requestEvent?.created in from..current }
            }
            to != null -> {
                eventStatusCache.cache.values.filter { it.requestEvent?.created in 0L..to }
            }
            else -> {
                eventStatusCache.cache.values
            }
        }
    }
}