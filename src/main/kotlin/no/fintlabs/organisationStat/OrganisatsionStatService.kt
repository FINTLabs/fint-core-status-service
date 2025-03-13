package no.fintlabs.organisationStat

import org.springframework.stereotype.Service

@Service
class OrganisatsionStatService(
    private val cache: OrganisationStatCache
){

    fun addError(orgId: String, consumer: String, errorMessage: String){
        cache.get(orgId)?.let {
            it.consumerErrors.getOrDefault(consumer, mutableListOf<String>().add(errorMessage))
            cache.add(orgId, it)
        }?: cache.add(orgId, OrganisastionStat(
            consumerErrors = mutableMapOf(orgId to listOf(errorMessage))
        )
        )
    }

}