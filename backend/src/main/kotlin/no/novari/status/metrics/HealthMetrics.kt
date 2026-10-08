package no.novari.status.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import no.novari.status.dashboard.DashboardService
import no.novari.status.health.ContractSnapshotService
import no.novari.status.health.FullSyncHealth
import no.novari.status.health.HeartbeatHealth
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Health as Prometheus gauges, refreshed on a schedule. 1 means healthy, 0 means a problem.
 * A muted contract always reports 1, so alerts built on these stop when it is muted and
 * start again when it is unmuted. fint.status.contract.muted shows which ones are muted.
 */
@Component
class HealthMetrics(
    meterRegistry: MeterRegistry,
    private val snapshotService: ContractSnapshotService,
    private val dashboardService: DashboardService,
) {
    private val heartbeat =
        MultiGauge
            .builder(
                "fint.status.heartbeat.ok",
            ).description("1 if the contract sends heartbeats")
            .register(meterRegistry)
    private val fullSync =
        MultiGauge
            .builder(
                "fint.status.full.sync.ok",
            ).description("1 if the capability has a recent enough full sync")
            .register(meterRegistry)
    private val muted =
        MultiGauge
            .builder(
                "fint.status.contract.muted",
            ).description("1 if the contract's warnings are muted")
            .register(meterRegistry)
    private val expired =
        MultiGauge
            .builder(
                "fint.status.events.expired",
            ).description("Expired events in the last 24 hours that count as problems")
            .register(meterRegistry)

    @Scheduled(
        fixedDelayString = "\${fint.status.metrics-interval:PT30S}",
        initialDelayString = "\${fint.status.metrics-initial-delay:PT10S}",
    )
    fun refresh() {
        val contracts = snapshotService.snapshot().contracts
        heartbeat.register(
            contracts.map { c ->
                MultiGauge.Row.of(c.tags(), if (c.muted || c.heartbeat != HeartbeatHealth.STOPPED) 1 else 0)
            },
            true,
        )
        fullSync.register(
            contracts.flatMap { c ->
                c.capabilities.map { cap ->
                    val ok = c.muted || cap.fullSync == FullSyncHealth.OK || cap.fullSync == FullSyncHealth.WAITING
                    MultiGauge.Row.of(
                        c.tags().and("domain", cap.domainName, "package", cap.packageName, "resource", cap.resourceName),
                        if (ok) 1 else 0,
                    )
                }
            },
            true,
        )
        muted.register(contracts.filter { it.muted }.map { MultiGauge.Row.of(it.tags(), 1) }, true)
        expired.register(
            dashboardService.dashboard().orgs.flatMap { main ->
                main.subOrgs.ifEmpty { listOf(main) }.map { org ->
                    MultiGauge.Row.of(Tags.of("org", org.orgId, "main_org", main.orgId), org.eventsExpired)
                }
            },
            true,
        )
    }

    private fun no.novari.status.health.ContractView.tags(): Tags = Tags.of("org", orgId, "main_org", mainOrgId, "username", username)
}
