package no.novari.status.contract

import no.novari.status.health.CapabilityView
import no.novari.status.health.ContractView
import no.novari.status.health.HeartbeatHealth
import no.novari.status.health.MuteView
import java.time.Instant

enum class ContractFilter { ALL, HEARTBEAT_STOPPED, HEARTBEAT_NEVER, FULL_SYNC_OVERDUE, FULL_SYNC_NEVER, MUTED }

enum class ContractSort { HEALTH, ORG, USERNAME, HEARTBEAT, FULL_SYNC, CAPABILITIES }

data class ContractSummary(
    val id: Long,
    val orgId: String,
    val mainOrgId: String,
    val username: String,
    val heartbeat: HeartbeatHealth,
    val lastHeartbeatAt: Instant?,
    val fullSyncOverdue: Int,
    val fullSyncNever: Int,
    val capabilities: Int,
    val muted: Boolean,
)

data class ContractPage(
    val items: List<ContractSummary>,
    val page: Int,
    val size: Int,
    val total: Long,
    val counts: Map<ContractFilter, Int>,
)

data class ContractDetail(
    val id: Long,
    val orgId: String,
    val mainOrgId: String,
    val username: String,
    val adapterId: String,
    val heartbeatIntervalMinutes: Int,
    val firstRegisteredAt: Instant,
    val registeredAt: Instant,
    val lastHeartbeatAt: Instant?,
    val heartbeat: HeartbeatHealth,
    val mute: MuteView?,
    val capabilities: List<CapabilityView>,
)

data class MuteRequest(
    val reason: String? = null,
    val mutedUntil: Instant? = null,
)

fun ContractView.toSummary() =
    ContractSummary(
        id = id,
        orgId = orgId,
        mainOrgId = mainOrgId,
        username = username,
        heartbeat = heartbeat,
        lastHeartbeatAt = lastHeartbeatAt,
        fullSyncOverdue = fullSyncOverdue,
        fullSyncNever = fullSyncNever,
        capabilities = capabilities.size,
        muted = muted,
    )

fun ContractView.toDetail() =
    ContractDetail(
        id = id,
        orgId = orgId,
        mainOrgId = mainOrgId,
        username = username,
        adapterId = adapterId,
        heartbeatIntervalMinutes = heartbeatIntervalMinutes,
        firstRegisteredAt = firstRegisteredAt,
        registeredAt = registeredAt,
        lastHeartbeatAt = lastHeartbeatAt,
        heartbeat = heartbeat,
        mute = mute,
        capabilities = capabilities,
    )
