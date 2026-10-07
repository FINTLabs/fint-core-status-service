package no.fintlabs.event.cache

import no.fintlabs.adapter.models.event.FintEvent
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.event.EventStatus
import no.fintlabs.event.response.ResponseFintEventConsumer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class EventStatusCache() {

    private val log = LoggerFactory.getLogger(ResponseFintEventConsumer::class.java)
    val cache: ConcurrentHashMap<String, EventStatus> = ConcurrentHashMap()


    fun add(fintEvent: FintEvent, topic: String) {
        cache.compute(fintEvent.corrId) { _, cachedEvent ->
            cachedEvent?.updateWith(fintEvent) ?: EventStatus.from(fintEvent, topic)
        }
    }

    private fun EventStatus.updateWith(event: FintEvent): EventStatus {
        return when (event) {
            is RequestFintEvent -> this.copyWith(requestEvent = event)
            is ResponseFintEvent -> this.copyWith(
                responseEvent = event,
                hasError = event.isFailed || event.isRejected || event.isConflicted
            )

            else -> throw IllegalArgumentException("Unsupported event type")
        }
    }

    private fun EventStatus.copyWith(
        topic: String = this.topic,
        hasError: Boolean = this.hasError,
        requestEvent: RequestFintEvent? = this.requestEvent,
        responseEvent: ResponseFintEvent? = this.responseEvent
    ): EventStatus {
        return EventStatus(
            topic = topic,
            corrId = this.corrId,
            orgId = this.orgId,
            hasError = hasError,
            requestEvent = requestEvent,
            responseEvent = responseEvent
        )
    }

}