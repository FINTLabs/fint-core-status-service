package no.fintlabs.event

import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class FintEventCache {

    val cache: ConcurrentHashMap<String, FintEvent> = ConcurrentHashMap()

    fun add(fintEvent: no.fintlabs.adapter.models.event.FintEvent) =
        cache.compute(fintEvent.corrId) { _, cachedEvent ->
            cachedEvent?.updateWith(fintEvent) ?: FintEvent.from(fintEvent)
        }

}

private fun FintEvent.updateWith(event: no.fintlabs.adapter.models.event.FintEvent): FintEvent {
    return when (event) {
        is RequestFintEvent -> this.copyWith(requestEvent = event)
        is ResponseFintEvent -> this.copyWith(
            responseEvent = event,
            hasError = event.isFailed || event.isRejected
        )

        else -> throw IllegalArgumentException("Unsupported event type")
    }
}

private fun FintEvent.copyWith(
    hasError: Boolean = this.hasError,
    requestEvent: RequestFintEvent? = this.requestEvent,
    responseEvent: ResponseFintEvent? = this.responseEvent
): FintEvent {
    return FintEvent(
        corrId = this.corrId,
        orgId = this.orgId,
        hasError = hasError,
        requestEvent = requestEvent,
        responseEvent = responseEvent
    )
}
