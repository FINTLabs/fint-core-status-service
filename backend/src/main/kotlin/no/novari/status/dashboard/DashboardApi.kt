package no.novari.status.dashboard

import java.time.Instant

data class OrgHealth(
    val orgId: String,
    val contracts: Int,
    val muted: Int,
    val heartbeatStopped: Int,
    val heartbeatNever: Int,
    val fullSyncOverdue: Int,
    val fullSyncNever: Int,
    val eventsExpired: Int,
    val eventsPendingLong: Int,
    val subOrgs: List<OrgHealth>,
) {
    val hasProblems: Boolean get() = heartbeatStopped > 0 || fullSyncOverdue > 0 || fullSyncNever > 0 || eventsExpired > 0
}

data class DashboardTotals(
    val heartbeatStopped: Int,
    val heartbeatNever: Int,
    val fullSyncOverdue: Int,
    val fullSyncNever: Int,
    val eventsExpired: Int,
    val eventsPendingLong: Int,
    val orgsWithProblems: Int,
)

data class Dashboard(
    val generatedAt: Instant,
    val totals: DashboardTotals,
    val orgs: List<OrgHealth>,
)
