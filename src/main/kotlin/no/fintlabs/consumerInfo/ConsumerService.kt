package no.fintlabs.consumerInfo

import kotlinx.coroutines.runBlocking
import no.fintlabs.organisationStat.OrganisationStatCache
import no.fintlabs.organisationStat.consumerInfo
import no.fintlabs.organisationStat.convertBytesToMegabytes
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ConsumerService(
    private val prometheusGateway: PrometheusGateway,
    private val organisationStatCache: OrganisationStatCache
) {

    private val log = LoggerFactory.getLogger(ConsumerService::class.java)

    suspend fun getAndMapConsumerInfo() {
        val data = prometheusGateway.getPodInfo()
        val restarts = prometheusGateway.getRestarts()
        data.data.result.forEach {
            it.metric
            organisationStatCache.add(
                it.metric.uid,
                consumerInfo(
                    it.metric.uid,
                    it.metric.pod,
                    it.metric.namespace,
                    "",
                    convertBytesToMegabytes(it.value.get(1))
                )
            )
        }

        restarts.data.result.forEach { restarts ->
            val consumerInfo = organisationStatCache.get(restarts.metric.uid)
            consumerInfo?.let {
                it.restarts = restarts.value.get(1)
            }
        }
    }

    fun getCache(): MutableMap<String, consumerInfo> {
        return organisationStatCache.cache
    }

    @Scheduled(cron = "0 * * * * *", initialDelay = 10000)
    fun populateCache() {
        runBlocking {
            getAndMapConsumerInfo()
            log.info("Populating cache for ${organisationStatCache.cache}")
        }
    }

}