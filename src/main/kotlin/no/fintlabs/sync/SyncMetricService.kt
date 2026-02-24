package no.fintlabs.sync

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.fintlabs.contract.model.Capability
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class SyncMetricService(
    private val meterRegistry: MeterRegistry
) {
    private val contractGauges = ConcurrentHashMap<String, AtomicInteger>()

    fun publishContractMetrics(capabilities: List<Capability>, orgId: String) {
        capabilities.forEach { cap ->
            val resource = cap.resourceName
            val key = "$orgId|$resource"

            val backing = contractGauges.computeIfAbsent(key) {
                val ai = AtomicInteger(0)
                Gauge.builder("adapter_contract_gauge") { ai.get().toDouble() }
                    .description("Adapter contract status")
                    .tags(Tags.of("org", orgId, "resource", resource))
                    .register(meterRegistry)
                ai
            }
            backing.set(if (cap.followsContract) 1 else 0)
        }
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