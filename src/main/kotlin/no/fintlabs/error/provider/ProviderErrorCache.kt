package no.fintlabs.error.provider

import no.fintlabs.status.models.error.ProviderError
import org.springframework.stereotype.Component

@Component
class ProviderErrorCache {

    private val errors: MutableList<ProviderError> = mutableListOf()

    fun getAll(): List<ProviderError> = errors.toList()
    fun add(error: ProviderError) = errors.add(error)

}