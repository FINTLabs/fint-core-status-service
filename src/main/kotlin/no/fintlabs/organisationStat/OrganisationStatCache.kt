package no.fintlabs.organisationStat

import org.springframework.stereotype.Repository

@Repository
class OrganisationStatCache {
    val cache = mutableMapOf<String, OrganisastionStat>()

    fun add(org: String, stat: OrganisastionStat) {
        cache[org] = stat
    }

    fun get(org: String): OrganisastionStat? {
        return cache[org]
    }
}