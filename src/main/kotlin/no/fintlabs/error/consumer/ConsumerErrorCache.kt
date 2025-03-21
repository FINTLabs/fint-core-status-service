package no.fintlabs.error.consumer

import no.fintlabs.status.models.error.ConsumerError
import org.springframework.stereotype.Component

@Component
class ConsumerErrorCache {

    private val orgErrorsMap: MutableMap<String, MutableList<ConsumerError>> = mutableMapOf()

    fun getAll(): List<ConsumerError> = orgErrorsMap.values.flatten()

    fun add(consumerError: ConsumerError) =
        orgErrorsMap.getOrPut(consumerError.org) { mutableListOf() }.add(consumerError)

    fun size() = orgErrorsMap.values.flatten().size
    fun size(org: String) = orgErrorsMap.getOrDefault(org, mutableListOf()).size

}