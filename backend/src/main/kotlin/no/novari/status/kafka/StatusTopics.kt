package no.novari.status.kafka

import no.novari.status.sync.SyncType

object StatusTopics {
    private const val PREFIX = "novari-no.fint-core.fint-felleskomponent-"

    const val CONTRACT = "${PREFIX}adapter-contract"
    const val HEARTBEAT = "${PREFIX}adapter-heartbeat"
    const val FULL_SYNC = "${PREFIX}adapter-full-sync"
    const val DELTA_SYNC = "${PREFIX}adapter-delta-sync"
    const val DELETE_SYNC = "${PREFIX}adapter-delete-sync"
    const val EVENT_REQUEST = "${PREFIX}event-request"
    const val EVENT_RESPONSE = "${PREFIX}event-response"

    fun syncTypeOf(topic: String): SyncType =
        when (topic) {
            FULL_SYNC -> SyncType.FULL
            DELTA_SYNC -> SyncType.DELTA
            DELETE_SYNC -> SyncType.DELETE
            else -> throw IllegalArgumentException("Not a sync topic: $topic")
        }
}
