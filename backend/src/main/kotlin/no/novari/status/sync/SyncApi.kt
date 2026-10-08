package no.novari.status.sync

import java.time.Instant

enum class SyncState { COMPLETED, RUNNING, STALLED }

data class SyncFilter(
    val type: SyncType?,
    val from: Instant,
    val to: Instant?,
    val state: SyncState?,
    val orgId: String?,
    val corrId: String?,
    val domainName: String?,
    val packageName: String?,
    val resourceName: String?,
    val startedAfter: Instant? = null,
)

data class SyncSummary(
    val corrId: String,
    val type: SyncType,
    val orgId: String,
    val domainName: String,
    val packageName: String,
    val resourceName: String,
    val adapterId: String,
    val totalPages: Int,
    val pagesReceived: Int,
    val totalSize: Long,
    val entitiesReceived: Long,
    val startedAt: Instant,
    val lastPageAt: Instant,
    val completedAt: Instant?,
    val state: SyncState,
)

data class SyncPageView(
    val page: Int,
    val pageSize: Int,
    val receivedAt: Instant,
)

data class SyncDetail(
    val sync: SyncSummary,
    val pages: List<SyncPageView>,
)
