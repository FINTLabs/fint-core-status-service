package no.novari.status.health

import java.time.Duration
import java.time.Instant

enum class HeartbeatHealth { OK, STOPPED, NEVER }

enum class FullSyncHealth { OK, WAITING, OVERDUE, NEVER }

/**
 * - A heartbeat has stopped when none has arrived for twice the interval the adapter registered.
 * - A full sync is overdue when the last completed one is older than the capability's interval.
 * - A capability that has never completed a full sync only counts as a problem once the
 *   contract has been registered for longer than that interval; before that it is waiting.
 */
object HealthRules {
    fun heartbeat(
        lastSeenAt: Instant?,
        intervalMinutes: Int,
        now: Instant,
    ): HeartbeatHealth =
        when {
            lastSeenAt == null -> HeartbeatHealth.NEVER
            Duration.between(lastSeenAt, now) > Duration.ofMinutes(2L * intervalMinutes.coerceAtLeast(1)) -> HeartbeatHealth.STOPPED
            else -> HeartbeatHealth.OK
        }

    fun fullSync(
        lastCompletedAt: Instant?,
        intervalDays: Int,
        firstRegisteredAt: Instant,
        now: Instant,
    ): FullSyncHealth {
        val interval = Duration.ofDays(intervalDays.coerceAtLeast(1).toLong())
        return when {
            lastCompletedAt != null && Duration.between(lastCompletedAt, now) > interval -> FullSyncHealth.OVERDUE
            lastCompletedAt != null -> FullSyncHealth.OK
            Duration.between(firstRegisteredAt, now) > interval -> FullSyncHealth.NEVER
            else -> FullSyncHealth.WAITING
        }
    }
}
