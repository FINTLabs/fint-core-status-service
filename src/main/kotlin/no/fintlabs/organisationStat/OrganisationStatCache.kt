package no.fintlabs.stats

import no.fintlabs.model.OrganisastionStat

class OrganisationStatCache() {
    val cache = mutableMapOf<String, OrganisastionStat>()

    fun add(org: String, stat: OrganisastionStat) {
        cache[org] = stat
    }

    fun get(org: String): OrganisastionStat? {
        return cache[org]
    }

}