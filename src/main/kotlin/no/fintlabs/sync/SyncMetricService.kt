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
            "domain", sync.domain,
            "package", sync.`package`,
            "resource", sync.resource,
            "org", sync.orgId,
            "type", sync.syncType
        ).increment()

    fun incrementFailedSyncs(sync: SyncMetadata) =
        meterRegistry.counter(
            "core.failed.syncs",
            "domain", sync.domain,
            "package", sync.`package`,
            "resource", sync.resource,
            "org", sync.orgId,
            "type", sync.syncType
        ).increment()

}