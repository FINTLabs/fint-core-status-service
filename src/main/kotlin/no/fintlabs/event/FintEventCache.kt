package no.fintlabs.event

import no.fintlabs.adapter.models.event.FintEvent
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class FintEventCache {

    val cache: ConcurrentHashMap<String, StatusEvent> = ConcurrentHashMap()

    fun add(fintEvent: FintEvent) =
        cache.compute(fintEvent.corrId) { _, cachedEvent ->
            cachedEvent?.updateWith(fintEvent) ?: StatusEvent.from(fintEvent)
        }

}

private fun StatusEvent.updateWith(event: FintEvent): StatusEvent {
    return when (event) {
        is RequestFintEvent -> this.copyWith(requestEvent = event)
        is ResponseFintEvent -> this.copyWith(
            responseEvent = event,
            hasError = event.isFailed || event.isRejected
        )

        else -> throw IllegalArgumentException("Unsupported event type")
    }
}

private fun StatusEvent.copyWith(
    hasError: Boolean = this.hasError,
    requestEvent: RequestFintEvent? = this.requestEvent,
    responseEvent: ResponseFintEvent? = this.responseEvent
): StatusEvent {
    return StatusEvent(
        corrId = this.corrId,
        orgId = this.orgId,
        hasError = hasError,
        requestEvent = requestEvent,
        responseEvent = responseEvent
    )
}
