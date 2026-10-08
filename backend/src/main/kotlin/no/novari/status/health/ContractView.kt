package no.novari.status.health

import java.time.Instant

data class MuteView(
    val mutedBy: String,
    val reason: String?,
    val mutedAt: Instant,
    val mutedUntil: Instant?,
)

data class CapabilityView(
    val domainName: String,
    val packageName: String,
    val resourceName: String,
    val fullSyncIntervalDays: Int,
    val deltaSyncInterval: String?,
    val lastFullSyncAt: Instant?,
    val fullSync: FullSyncHealth,
)

data class ContractView(
    val id: Long,
    val username: String,
    val orgId: String,
    val mainOrgId: String,
    val adapterId: String,
    val heartbeatIntervalMinutes: Int,
    val firstRegisteredAt: Instant,
    val registeredAt: Instant,
    val lastHeartbeatAt: Instant?,
    val heartbeat: HeartbeatHealth,
    val capabilities: List<CapabilityView>,
    val mute: MuteView?,
) {
    val muted: Boolean get() = mute != null
    val fullSyncOverdue: Int get() = capabilities.count { it.fullSync == FullSyncHealth.OVERDUE }
    val fullSyncNever: Int get() = capabilities.count { it.fullSync == FullSyncHealth.NEVER }
    val hasProblem: Boolean get() = !muted && (heartbeat == HeartbeatHealth.STOPPED || fullSyncOverdue > 0 || fullSyncNever > 0)

    fun serves(
        orgId: String,
        domainName: String?,
        packageName: String?,
        resourceName: String?,
    ): Boolean =
        this.orgId == orgId &&
            capabilities.any { it.domainName == domainName && it.packageName == packageName && it.resourceName == resourceName }
}

data class ContractSnapshot(
    val contracts: List<ContractView>,
    val takenAt: Instant,
) {
    fun candidatesFor(
        orgId: String,
        domainName: String?,
        packageName: String?,
        resourceName: String?,
    ): List<ContractView> = contracts.filter { it.serves(orgId, domainName, packageName, resourceName) }
}
