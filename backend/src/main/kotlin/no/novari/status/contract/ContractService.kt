package no.novari.status.contract

import no.novari.status.health.ContractSnapshotService
import no.novari.status.health.ContractView
import no.novari.status.health.HeartbeatHealth
import no.novari.status.web.PageRequest
import no.novari.status.web.SortDirection
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Clock

@Service
class ContractService(
    private val snapshotService: ContractSnapshotService,
    private val muteStore: MuteStore,
    private val clock: Clock,
) {
    fun list(
        query: String?,
        filter: ContractFilter,
        sort: ContractSort,
        direction: SortDirection,
        page: PageRequest,
    ): ContractPage {
        val contracts = snapshotService.snapshot().contracts
        val searched = contracts.filter { it.matches(query) }
        val shown = searched.filter { it.passes(filter) }.sortedWith(comparatorFor(sort, direction))
        return ContractPage(
            items = shown.drop(page.offset.toInt()).take(page.size).map { it.toSummary() },
            page = page.page,
            size = page.size,
            total = shown.size.toLong(),
            counts = ContractFilter.entries.associateWith { f -> searched.count { it.passes(f) } },
        )
    }

    fun get(id: Long): ContractDetail = find(id).toDetail()

    fun mute(
        id: Long,
        request: MuteRequest,
        mutedBy: String,
    ): ContractDetail {
        val contract = find(id)
        val now = clock.instant()
        if (request.mutedUntil != null && !request.mutedUntil.isAfter(now)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "mutedUntil must be in the future")
        }
        muteStore.save(contract.username, contract.orgId, mutedBy, request.reason, request.mutedUntil, now)
        return get(id)
    }

    fun unmute(id: Long) {
        val contract = find(id)
        muteStore.delete(contract.username, contract.orgId)
    }

    private fun find(id: Long): ContractView =
        snapshotService.snapshot().contracts.firstOrNull { it.id == id }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No contract with id $id")

    private fun ContractView.matches(query: String?): Boolean {
        val q = query?.trim()?.lowercase().orEmpty()
        return q.isEmpty() || listOf(orgId, username, adapterId).any { it.lowercase().contains(q) }
    }

    private fun ContractView.passes(filter: ContractFilter): Boolean =
        when (filter) {
            ContractFilter.ALL -> true
            ContractFilter.HEARTBEAT_STOPPED -> !muted && heartbeat == HeartbeatHealth.STOPPED
            ContractFilter.HEARTBEAT_NEVER -> !muted && heartbeat == HeartbeatHealth.NEVER
            ContractFilter.FULL_SYNC_OVERDUE -> !muted && fullSyncOverdue > 0
            ContractFilter.FULL_SYNC_NEVER -> !muted && fullSyncNever > 0
            ContractFilter.MUTED -> muted
        }

    private fun comparatorFor(
        sort: ContractSort,
        direction: SortDirection,
    ): Comparator<ContractView> {
        val primary: Comparator<ContractView> =
            when (sort) {
                ContractSort.HEALTH -> compareBy { it.healthRank() }
                ContractSort.ORG -> compareBy<ContractView> { it.mainOrgId }.thenBy { if (it.orgId == it.mainOrgId) "" else it.orgId }
                ContractSort.USERNAME -> compareBy { it.username }
                ContractSort.HEARTBEAT -> compareBy { HEARTBEAT_RANK.getValue(it.heartbeat) }
                ContractSort.FULL_SYNC -> compareByDescending { it.fullSyncOverdue + it.fullSyncNever }
                ContractSort.CAPABILITIES -> compareBy { it.capabilities.size }
            }
        val directed = if (direction == SortDirection.DESC) primary.reversed() else primary
        return directed.thenBy { it.orgId }.thenBy { it.username }
    }

    private fun ContractView.healthRank(): Int =
        when {
            muted -> 5
            heartbeat == HeartbeatHealth.STOPPED -> 0
            fullSyncNever > 0 -> 1
            fullSyncOverdue > 0 -> 2
            heartbeat == HeartbeatHealth.NEVER -> 3
            else -> 4
        }

    companion object {
        private val HEARTBEAT_RANK = mapOf(HeartbeatHealth.STOPPED to 0, HeartbeatHealth.NEVER to 1, HeartbeatHealth.OK to 2)
    }
}
