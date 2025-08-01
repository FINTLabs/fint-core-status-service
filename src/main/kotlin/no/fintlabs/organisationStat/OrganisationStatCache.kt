package no.fintlabs.organisationStat

import no.fintlabs.consumerInfo.consumerInfo
import org.springframework.stereotype.Repository

@Repository
class OrganisationStatCache {
    val cache = mutableMapOf<String, consumerInfo>()

    fun add(org: String, stat: consumerInfo) {
        cache[org] = stat
    }

    fun get(org: String): consumerInfo? {
        return cache[org]
    }

    fun getAll(): List<consumerInfo> {
        return cache.values.toList()
    }
}