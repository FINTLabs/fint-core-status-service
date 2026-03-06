package no.fintlabs.event.response

import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.adapter.models.sync.SyncPageEntry

@Entity
class ResponseFintEventEntity(
    @Id var corrId: String?,
    var orgId: String?,
    var adapterId: String?,
    var topic: String?,
    var handledAt: Long,
    var failed: Boolean,
    var errorMessage: String?,
    var rejectReason: String?,
    var conflicted: Boolean,
    var rejected: Boolean,
    var conflictReason: String?,
    var syncPageEntryIdentifier: String?,
    var syncPageEntryResource: String?
) {
    constructor() : this(null, null, null, null, 0L, false, null, null, false, false, null, null, null)

    fun toResponseEvent(): ResponseFintEvent {
        return ResponseFintEvent.builder()
            .corrId(corrId)
            .orgId(orgId)
            .adapterId(adapterId)
            .handledAt(handledAt)
            .failed(failed)
            .errorMessage(errorMessage)
            .rejected(rejected)
            .rejectReason(rejectReason)
            .conflicted(conflicted)
            .conflictReason(conflictReason)
            .value(SyncPageEntry.of(syncPageEntryIdentifier, syncPageEntryResource))
            .build()
    }

    companion object {
        fun fromResponseEvent(responseEvent: ResponseFintEvent, topic: String): ResponseFintEventEntity {
            return ResponseFintEventEntity(
                responseEvent.corrId,
                responseEvent.orgId,
                responseEvent.adapterId,
                topic,
                responseEvent.handledAt,
                responseEvent.isFailed,
                responseEvent.errorMessage,
                responseEvent.rejectReason,
                responseEvent.isConflicted,
                responseEvent.isRejected,
                responseEvent.conflictReason,
                responseEvent.value?.identifier ?: "[NULL]",
                ""
            )
        }
    }
}
