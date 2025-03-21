package no.fintlabs.sync

import io.micrometer.core.instrument.MeterRegistry
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component

@Component
class SyncMetric(
    private val meterRegistry: MeterRegistry
) {

    fun incrementCompletedSyncs(sync: SyncMetadata) =
        meterRegistry.counter(
            "core.completed.syncs",
            "component", "${sync.domain} ${sync.`package`}",
            "resource", sync.resource
        ).increment()

    fun incrementFailedSyncs(sync: SyncMetadata) =
        meterRegistry.counter(
            "core.failed.syncs",
            "component", "${sync.domain} ${sync.`package`}",
            "resource", sync.resource
        ).increment()

}