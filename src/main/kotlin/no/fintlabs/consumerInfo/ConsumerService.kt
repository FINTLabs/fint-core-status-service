package no.fintlabs.consumerInfo

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ConsumerService(
    private val prometheusGateway: PrometheusGateway,
    private val consumerInfoCache: ConsumerInfoCache,
) {

    private val log = LoggerFactory.getLogger(ConsumerService::class.java)

    suspend fun getAndMapConsumerInfo() {
        val data = prometheusGateway.getPodInfo()
        val restarts = prometheusGateway.getRestarts()

        val podToRestarts: Map<String, String> = restarts.data.result.associate {
            it.metric.pod to it.value[1]
        }

        val consumers = data.data.result.map {
            val pod = it.metric.pod
            consumerInfo(
                uid = it.metric.uid,
                podName = pod,
                organisation = it.metric.namespace,
                restarts = podToRestarts[pod] ?: "0",
                memoryUsage = it.value[1]
            )
        }

        val groupedByOrg = consumers.groupBy { it.organisation }

        groupedByOrg.forEach { (org, list) ->
            consumerInfoCache.cache.put(org, list)
        }

    }

    fun getCache(): MutableMap<String, List<consumerInfo>> {
        return consumerInfoCache.cache
    }

    @Scheduled(cron = "0 * * * * *")
    @ConditionalOnProperty(
        name = ["fintlabs.prometheus.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun populateCache() {
        runBlocking {
            getAndMapConsumerInfo()
            log.info("Populating cache for ${consumerInfoCache.cache}")
        }
    }

}