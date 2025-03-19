package no.fintlabs.sync

import kotlinx.coroutines.*
import no.fintlabs.sync.kafka.CompletedFullSyncProducer
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Service
class SyncProgressionService(
    private val syncMetric: SyncMetric,
    private val syncProducer: CompletedFullSyncProducer
) {

    private val timeoutJobs: MutableMap<String, Job> = ConcurrentHashMap()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    fun processPageProgression(sync: SyncMetadata) =
        if (sync.finished) {
            syncProducer.publishCompletedFullSync(sync)
            syncMetric.incrementCompletedSyncs(sync)
        } else scheduleTimeout(sync)

    fun scheduleTimeout(sync: SyncMetadata) {
        cancelTimeout(sync.corrId)
        timeoutJobs[sync.corrId] = coroutineScope.launch {
            delay(Duration.ofMinutes(3).toMillis())
            if (sync.finished.not()) syncMetric.incrementFailedSyncs(sync)
        }
    }

    private fun cancelTimeout(corrId: String) = timeoutJobs.remove(corrId)?.cancel()

}
