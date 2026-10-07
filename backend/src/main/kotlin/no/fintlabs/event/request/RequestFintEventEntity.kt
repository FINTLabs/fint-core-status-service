package no.fintlabs.event.request

import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.operation.OperationType

@Entity
class RequestFintEventEntity(
    @Id var corrId: String,
    var orgId: String,
    var topic: String,
    var domainName: String,
    var packageName: String,
    var resourceName: String,
    var operationType: String,
    var created: Long,
    var timeToLive: Long,
    var value: String
) {
    constructor() : this("", "", "", "", "", "", "", 0L, 0L, "")

    fun toRequestEvent(): RequestFintEvent {
        return RequestFintEvent.builder()
            .corrId(corrId)
            .orgId(orgId)
            .domainName(domainName)
            .packageName(packageName)
            .resourceName(resourceName)
            .operationType(parseOperationType(operationType))
            .created(created)
            .timeToLive(timeToLive)
            .value(value)
            .build()
    }

    private fun parseOperationType(operationTypeValue: String?): OperationType {
        return when (operationTypeValue) {
            "CREATE" -> OperationType.CREATE
            "UPDATE" -> OperationType.UPDATE
            "VALIDATE" -> OperationType.VALIDATE
            else -> OperationType.UNKNOWN
        }
    }

    companion object {
        fun fromRequestEvent(requestEvent: RequestFintEvent, topic: String): RequestFintEventEntity {
            return RequestFintEventEntity(
                requestEvent.corrId,
                requestEvent.orgId,
                topic,
                requestEvent.domainName,
                requestEvent.packageName,
                requestEvent.resourceName,
                requestEvent.operationType.name,
                requestEvent.created,
                requestEvent.timeToLive,
                ""
            )
        }
    }
}