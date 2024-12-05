package no.fintlabs.event

import no.fintlabs.adapter.models.event.FintEvent
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent

class EventStatus(
    val topic: String,
    val corrId: String,
    val orgId: String,
    var hasError: Boolean,
    var requestEvent: RequestFintEvent?,
    var responseEvent: ResponseFintEvent?
) {

    companion object {
        fun from(event: FintEvent, topic: String): EventStatus {
            return when (event) {
                is RequestFintEvent -> {
                    EventStatus(
                        topic = topic,
                        corrId = event.corrId,
                        orgId = event.orgId,
                        hasError = false,
                        requestEvent = event,
                        responseEvent = null
                    )
                }

                is ResponseFintEvent -> {
                    EventStatus(
                        topic = topic,
                        corrId = event.corrId,
                        orgId = event.orgId,
                        hasError = event.isFailed || event.isRejected,
                        requestEvent = null,
                        responseEvent = event
                    )
                }

                else -> throw IllegalArgumentException("Unsupported event type")
            }
        }
    }
}
