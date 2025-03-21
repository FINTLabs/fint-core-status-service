package no.fintlabs.error.provider

import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.status.models.error.ProviderError
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class ProviderErrorConsumer {

    @Bean
    fun registerProviderErrorConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, ProviderError> {
        return eventConsumerFactoryService.createFactory(
            ProviderError::class.java,
            this::processEvent
        ).createContainer(
            EventTopicNameParameters.builder()
                .orgId("fintlabs-no")
                .domainContext("fint-core")
                .eventName("provider-error")
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, ProviderError>) {

    }
}