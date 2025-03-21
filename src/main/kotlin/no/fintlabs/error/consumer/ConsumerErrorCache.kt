package no.fintlabs.error.consumer

import no.fintlabs.status.models.error.ConsumerError
import org.springframework.stereotype.Component

@Component
class ConsumerErrorCache {

    private val orgErrorsMap: MutableMap<String, MutableList<ConsumerError>> = mutableMapOf()

    fun add(org: String, consumerError: ConsumerError) =
        orgErrorsMap.getOrPut(org) { mutableListOf() }.add(consumerError)
    
    fun size() = orgErrorsMap.size
    fun size(org: String) = orgErrorsMap.getOrDefault(org, mutableListOf()).size

}