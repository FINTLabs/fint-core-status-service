package no.fintlabs.sync

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class SyncMetricService(
    private val meterRegistry: MeterRegistry
) {

    private final val absentFullSyncs = AtomicInteger(0)

    fun incrementAbsentFullsyncs() = absentFullSyncs.getAndIncrement()
    fun decrementAbsentFullsyncs() = absentFullSyncs.getAndDecrement()

    init {
        Gauge.builder("core.absent.full-syncs", absentFullSyncs) { it.get().toDouble() }
            .description("Absent full syncs for capabilities")
            .register(meterRegistry)
    }

    fun incrementCompletedSyncs(sync: SyncMetadata) =
        meterRegistry.counter(
            "core.completed.syncs",
            "domain", sync.domain,
            "package", sync.`package`,
            "resource", sync.resource,
            "org", sync.orgId,
            "type", sync.syncType.name.lowercase()
        ).increment()


    fun incrementFailedSyncs(sync: SyncMetadata) =
        meterRegistry.counter(
            "core.failed.syncs",
            "domain", sync.domain,
            "package", sync.`package`,
            "resource", sync.resource,
            "org", sync.orgId,
            "type", sync.syncType.name.lowercase()
        ).increment()

}