package no.fintlabs.error.consumer

import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.status.models.error.ConsumerError
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class ConsumerErrorConsumer(
    private val consumerErrorCache: ConsumerErrorCache,
) {

    @Bean
    fun registerConsumerErrorConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, ConsumerError> {
        return eventConsumerFactoryService.createFactory(
            ConsumerError::class.java,
            this::processEvent
        ).createContainer(
            EventTopicNameParameters.builder()
                .orgId("fintlabs-no")
                .domainContext("fint-core")
                .eventName("consumer-error")
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, ConsumerError>) {
        val consumerError = consumerRecord.value()
        consumerErrorCache.add(consumerError.org, consumerError)
    }
}