package no.fintlabs.event

import no.fintlabs.adapter.models.event.FintEvent
import org.springframework.stereotype.Service

@Service
class FintEventService(val fintEventCache: FintEventCache) {

    fun lol(event: FintEvent) {
        fintEventCache.add(event)
    }

    fun getAllEvents(): Collection<EventStatus> {
        return fintEventCache.cache.values.toList()
    }

    fun getEventById(id: String): EventStatus? {
        return fintEventCache.cache[id];
    }

}