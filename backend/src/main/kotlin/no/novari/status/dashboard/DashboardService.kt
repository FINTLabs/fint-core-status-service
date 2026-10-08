package no.novari.status.dashboard

import no.novari.status.event.EventQueries
import no.novari.status.event.EventRow
import no.novari.status.event.EventStatus
import no.novari.status.health.ContractSnapshot
import no.novari.status.health.ContractSnapshotService
import no.novari.status.health.ContractView
import no.novari.status.health.HeartbeatHealth
import no.novari.status.org.OrgTree
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * One row per main org, summing the org and its sub-orgs, with one row per member org
 * under it. Muted contracts are counted but are never problems. An expired event only
 * counts when at least one contract that could have answered it is not muted, or when no
 * contract could have answered it at all.
 */
@Service
class DashboardService(
    private val snapshotService: ContractSnapshotService,
    private val eventQueries: EventQueries,
) {
    fun dashboard(): Dashboard {
        val snapshot = snapshotService.snapshot()
        val now = snapshot.takenAt
        val events =
            eventQueries
                .findUnanswered(expiredSince = now.minus(EXPIRED_WINDOW), pendingBefore = now.minus(PENDING_LONG))
                .filter { it.counts(snapshot) }
        val tree = OrgTree(snapshot.contracts.map { it.orgId } + events.map { it.orgId })

        val orgs =
            tree
                .mainOrgs()
                .map { main ->
                    val members = tree.membersOf(main)
                    val total = health(main, snapshot.contracts.filter { it.orgId in members }, events.filter { it.orgId in members })
                    val subOrgs =
                        if (members.size > 1) {
                            members.map { org ->
                                health(org, snapshot.contracts.filter { it.orgId == org }, events.filter { it.orgId == org })
                            }
                        } else {
                            emptyList()
                        }
                    total.copy(subOrgs = subOrgs)
                }.sortedWith(compareByDescending<OrgHealth> { it.hasProblems }.thenBy { it.orgId })

        return Dashboard(
            generatedAt = now,
            totals =
                DashboardTotals(
                    heartbeatStopped = orgs.sumOf { it.heartbeatStopped },
                    heartbeatNever = orgs.sumOf { it.heartbeatNever },
                    fullSyncOverdue = orgs.sumOf { it.fullSyncOverdue },
                    fullSyncNever = orgs.sumOf { it.fullSyncNever },
                    eventsExpired = orgs.sumOf { it.eventsExpired },
                    eventsPendingLong = orgs.sumOf { it.eventsPendingLong },
                    orgsWithProblems = orgs.count { it.hasProblems },
                ),
            orgs = orgs,
        )
    }

    private fun health(
        orgId: String,
        contracts: List<ContractView>,
        events: List<EventRow>,
    ): OrgHealth {
        val live = contracts.filterNot { it.muted }
        return OrgHealth(
            orgId = orgId,
            contracts = contracts.size,
            muted = contracts.size - live.size,
            heartbeatStopped = live.count { it.heartbeat == HeartbeatHealth.STOPPED },
            heartbeatNever = live.count { it.heartbeat == HeartbeatHealth.NEVER },
            fullSyncOverdue = live.sumOf { it.fullSyncOverdue },
            fullSyncNever = live.sumOf { it.fullSyncNever },
            eventsExpired = events.count { it.status == EventStatus.EXPIRED },
            eventsPendingLong = events.count { it.status == EventStatus.PENDING },
            subOrgs = emptyList(),
        )
    }

    private fun EventRow.counts(snapshot: ContractSnapshot): Boolean {
        val candidates = snapshot.candidatesFor(orgId, domainName, packageName, resourceName)
        return candidates.isEmpty() || candidates.any { !it.muted }
    }

    companion object {
        val EXPIRED_WINDOW: Duration = Duration.ofHours(24)
        val PENDING_LONG: Duration = Duration.ofMinutes(5)
    }
}
