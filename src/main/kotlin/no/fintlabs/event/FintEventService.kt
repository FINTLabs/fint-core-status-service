package no.fintlabs.event

import org.springframework.stereotype.Service

@Service
class FintEventService(val fintEventCache: FintEventCache) {

    fun getAllEvents(): Collection<FintEvent> {
        return fintEventCache.cache.values.toList()
    }

    fun getEventById(id: String): FintEvent? {
        return fintEventCache.cache[id];
    }

}