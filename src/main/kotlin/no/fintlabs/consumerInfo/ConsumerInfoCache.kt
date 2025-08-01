package no.fintlabs.consumerInfo

import org.springframework.stereotype.Repository

@Repository
class ConsumerInfoCache {
    val cache = mutableMapOf<String, List<consumerInfo>>()

    fun get(org: String) = cache[org]

}