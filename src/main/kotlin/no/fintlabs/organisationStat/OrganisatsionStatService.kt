package no.fintlabs.organisationStat

import org.springframework.stereotype.Service

@Service
class OrganisatsionStatService(
    private val cache: OrganisationStatCache
) {

    fun addError(orgId: String, consumer: String, errorMessage: String) {
        cache.get(orgId)?.apply {
            errors += 1
        } ?: cache.add(orgId, OrganisastionStat(errors = 1))
    }

}