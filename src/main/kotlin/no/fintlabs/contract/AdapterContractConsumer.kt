package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component


@Component
class AdapterContractConsumer(
    val adapterContextCache: AdapterContextCache
) {

    @Bean
    fun registerAdapterContractKafkaConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, AdapterContract> {
        return eventConsumerFactoryService.createFactory(
            AdapterContract::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNameParameters
                .builder()
                .eventName("adapter-register")
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, AdapterContract>) {
        adapterContextCache.add(consumerRecord.value())
    }

}