package no.fintlabs.errors

import no.fintlabs.organisationStat.OrganisastionStat
import no.fintlabs.organisationStat.OrganisationStatCache
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PrometheusService(
    private val prometheusGateway: PrometheusGateway,
    private val organisationStatCache: OrganisationStatCache
) {

    val log = LoggerFactory.getLogger(PrometheusService::class.java)

    suspend fun getPodInfo() {
        val data = prometheusGateway.fetchPrometheusData()
        data.data.result.forEach {
            it.metric
            organisationStatCache.add(it.metric.namespace, OrganisastionStat(it.value[1].toInt()))
        }
    }

    fun getCache(): MutableMap<String, OrganisastionStat> {
        return organisationStatCache.cache
    }

    @Scheduled(cron = "0 * * * * *")
    suspend fun populateCache() {
        log.info("Populating cache")
//        getPodInfo() disabled due to bug
    }

}