import kotlinx.coroutines.*
import no.fintlabs.sync.SyncMetric
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Service
class SyncTimeoutService(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val syncMetric: SyncMetric
) {

    private val timeoutJobs: MutableMap<String, Job> = ConcurrentHashMap()

    fun scheduleTimeout(sync: SyncMetadata) {
        cancelTimeout(sync.corrId)
        val job = coroutineScope.launch {
            delay(Duration.ofMinutes(3).toMillis())
            if (sync.finished.not()) syncMetric.incrementFailedSyncs(sync)
        }
        timeoutJobs[sync.corrId] = job
    }

    fun cancelTimeout(corrId: String) = timeoutJobs.remove(corrId)?.cancel()

}
