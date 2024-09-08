package no.fintlabs.contract.heartbeat

import no.fintlabs.adapter.models.AdapterHeartbeat
import org.springframework.stereotype.Component

@Component
class HeartbeatCache {

    private val cache: MutableMap<String, Long> = mutableMapOf()

    fun add(adapterHeartbeat: AdapterHeartbeat) = cache.put(adapterHeartbeat.adapterId, adapterHeartbeat.time)

    fun getLastHeartbeat(adapterId: String) = cache[adapterId]

}