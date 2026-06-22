package no.fintlabs.event

import no.fintlabs.event.cache.EventStatusCache
import org.springframework.stereotype.Service
import java.util.*

@Service
class FintEventService(val eventStatusCache: EventStatusCache) {


    fun getEventsMetrics(): Map<String, Map<String, Int>> {
        val all = eventStatusCache.getAll()
        return mapOf(
            "EventsMetrics" to mapOf(
            "total" to all.size,
            "errors" to all.count { it.hasError }
            )
        )
    }

    fun getEventById(id: String): EventStatus? {
        return eventStatusCache.cache[id]
    }

    fun getEventsByTime(from: Long?, to: Long?): Collection<EventStatus> {
        val aDayAgo = 24 * 60 * 60 * 1000
        val current = Date().time
        val defaultFrom = current - aDayAgo

        val f = from ?: defaultFrom
        val t = to ?: current

        return eventStatusCache.cache.values.filter {
            val created = it.requestEvent?.created ?: return@filter false
            created in f..t
        }
    }
}