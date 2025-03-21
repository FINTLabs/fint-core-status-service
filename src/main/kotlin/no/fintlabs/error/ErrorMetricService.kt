package no.fintlabs.error

import io.micrometer.core.instrument.MeterRegistry
import no.fintlabs.status.models.error.ConsumerError
import no.fintlabs.status.models.error.ProviderError
import org.springframework.stereotype.Service

@Service
class ErrorMetricService(
    private val meterRegistry: MeterRegistry
) {

    fun incrementConsumerError(consumerError: ConsumerError) =
        this.meterRegistry.counter(
            "core.consumer.error",
            "domain", consumerError.domain,
            "package", consumerError.pkg,
            "org", consumerError.org,
            "name", consumerError.name
        ).increment()

    fun incrementProviderError(providerError: ProviderError) =
        meterRegistry.counter(
            "core.provider.error",
            "name", providerError.name
        ).increment()

}
