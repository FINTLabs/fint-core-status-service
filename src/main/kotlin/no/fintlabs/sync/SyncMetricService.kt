package no.fintlabs.sync

import io.micrometer.core.instrument.MeterRegistry
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component

@Component
class SyncMetricService(
    private val meterRegistry: MeterRegistry
) {

    fun incrementCompletedSyncs(sync: SyncMetadata) =
        meterRegistry.counter(
            "core.completed.syncs",
            "app", "${sync.domain}-${sync.`package`}",
            "resource", sync.resource,
            "org", sync.orgId
        ).increment()

    fun incrementFailedSyncs(sync: SyncMetadata) =
        meterRegistry.counter(
            "core.failed.syncs",
            "app", "${sync.domain}-${sync.`package`}",
            "resource", sync.resource,
            "org", sync.orgId
        ).increment()

}