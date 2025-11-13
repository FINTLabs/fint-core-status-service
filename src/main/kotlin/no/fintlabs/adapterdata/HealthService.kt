package no.fintlabs.adapterdata

import com.nimbusds.jose.util.health.HealthStatus
import no.fintlabs.contract.model.Contract
import org.springframework.stereotype.Service

@Service
class HealthService {

    fun calculateHealth(contract: Contract): Enum<HealthStatus> {
        return HealthStatus.HEALTHY
    }

    fun calculateHealth(contract: Contract, adapterId: String) = true

}