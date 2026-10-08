package no.novari.status.kafka

data class ContractMessage(
    val adapterId: String,
    val orgId: String,
    val username: String,
    val heartbeatIntervalInMinutes: Int,
    val capabilities: Set<CapabilityMessage> = emptySet(),
    val eventCapabilities: Set<EventCapabilityMessage>? = null,
)

data class EventCapabilityMessage(
    val domainName: String,
    val packageName: String,
    val resourceName: String,
    val operations: Set<String> = emptySet(),
)

data class CapabilityMessage(
    val domainName: String,
    val packageName: String,
    val resourceName: String,
    val fullSyncIntervalInDays: Int,
    val deltaSyncInterval: String? = null,
)

data class HeartbeatMessage(
    val adapterId: String,
    val orgId: String,
    val username: String,
)

data class SyncPageMessage(
    val corrId: String,
    val adapterId: String,
    val orgId: String,
    val totalSize: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val uriRef: String,
)

data class RequestEventMessage(
    val corrId: String,
    val orgId: String,
    val domainName: String? = null,
    val packageName: String? = null,
    val resourceName: String? = null,
    val operationType: String? = null,
    val created: Long? = null,
    val timeToLive: Long? = null,
)

data class ResponseEventMessage(
    val corrId: String,
    val orgId: String,
    val adapterId: String? = null,
    val handledAt: Long? = null,
    val failed: Boolean = false,
    val errorMessage: String? = null,
    val rejected: Boolean = false,
    val rejectReason: String? = null,
    val conflicted: Boolean = false,
    val conflictReason: String? = null,
)
