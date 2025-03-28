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


    fun incrementCompletedSyncs(sync: SyncMetadata) {
        val tags = arrayOf(
            "app", "${sync.domain}-${sync.`package`}",
            "resource", sync.resource,
            "org", sync.orgId
        )

        meterRegistry.counter("core.completed.syncs", *tags).increment()
    }


    fun incrementFailedSyncs(sync: SyncMetadata) {
        val tags = arrayOf(
            "app", "${sync.domain}-${sync.`package`}",
            "resource", sync.resource,
            "org", sync.orgId
        )
        
        meterRegistry.counter("core.failed.syncs", *tags).increment()
    }


}