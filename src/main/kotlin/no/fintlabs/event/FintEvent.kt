package no.fintlabs.event

import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent

class FintEvent(
    val corrId: String,
    val orgId: String,
    var hasError: Boolean,
    var requestEvent: RequestFintEvent?,
    var responseEvent: ResponseFintEvent?
) {

    companion object {
        fun from(event: no.fintlabs.adapter.models.event.FintEvent): FintEvent {
            return when (event) {
                is RequestFintEvent -> {
                    FintEvent(
                        corrId = event.corrId,
                        orgId = event.orgId,
                        hasError = false,
                        requestEvent = event,
                        responseEvent = null
                    )
                }

                is ResponseFintEvent -> {
                    FintEvent(
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