package no.fintlabs.event.cache

import no.fintlabs.MappingService
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.event.response.ResponseFintEventConsumer
import no.fintlabs.request.RequestFintEventEntity
import no.fintlabs.request.RequestFintEventJpaRepository
import no.fintlabs.response.ResponseFintEventEntity
import no.fintlabs.response.ResponseFintEventJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EventStatusCacheService(
    private val requestFintEventJpaRepository: RequestFintEventJpaRepository,
    private val responseFintEventJpaRepository: ResponseFintEventJpaRepository,
    private val eventStatusCache: EventStatusCache,
    private val mapper: MappingService
) {

    private val log = LoggerFactory.getLogger(ResponseFintEventConsumer::class.java)

    init {
        fillCache()
    }

    @Scheduled(cron = "0 08 13 * * ?")
    private fun flushAndRefiilCache() {
        eventStatusCache.cache.clear()
        fillCache()
    }

    fun fillCache() {
        val requests: List<RequestFintEventEntity> = requestFintEventJpaRepository.findAll()
        val responses: List<ResponseFintEventEntity> = responseFintEventJpaRepository.findAll()

        requests.forEach { requestEntity ->
            val requestFintEvent: RequestFintEvent = mapper.mapEntityToRequestFintEvent(requestEntity)
            eventStatusCache.add(requestFintEvent, requestEntity.topic)
        }
        log.info("Requests synced from database, count = ${requests.size}")

        responses.forEach { responseEntity ->
            val responseFintEvent: ResponseFintEvent = mapper.mapEntityToResponseFintEvent(responseEntity)
            eventStatusCache.add(responseFintEvent, responseEntity.topic)
        }
        log.info("Responses synced from database, count = ${responses.size}")
    }

}