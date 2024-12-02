package no.fintlabs.event

import no.fintlabs.adapter.models.event.FintEvent
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class EventStatusCache {

    val cache: ConcurrentHashMap<String, EventStatus> = ConcurrentHashMap()

    fun add(fintEvent: FintEvent) =
        cache.compute(fintEvent.corrId) { _, cachedEvent ->
            cachedEvent?.updateWith(fintEvent) ?: EventStatus.from(fintEvent)
        }

}

private fun EventStatus.updateWith(event: FintEvent): EventStatus {
    return when (event) {
        is RequestFintEvent -> this.copyWith(requestEvent = event)
        is ResponseFintEvent -> this.copyWith(
            responseEvent = event,
            hasError = event.isFailed || event.isRejected
        )

        else -> throw IllegalArgumentException("Unsupported event type")
    }
}

private fun EventStatus.copyWith(
    hasError: Boolean = this.hasError,
    requestEvent: RequestFintEvent? = this.requestEvent,
    responseEvent: ResponseFintEvent? = this.responseEvent
): EventStatus {
    return EventStatus(
        corrId = this.corrId,
        orgId = this.orgId,
        hasError = hasError,
        requestEvent = requestEvent,
        responseEvent = responseEvent
    )
}
