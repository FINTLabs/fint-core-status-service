package no.fintlabs.contract.heartbeat

import no.fintlabs.adapter.models.AdapterHeartbeat
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class HeartbeatConsumer(val heartbeatCache: HeartbeatCache) {

    @Bean
    fun registerAdapterHeartbeatKafkaConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, AdapterHeartbeat> {
        return eventConsumerFactoryService.createFactory(
            AdapterHeartbeat::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNameParameters
                .builder()
                .eventName("")
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, AdapterHeartbeat>) {
        heartbeatCache.add(consumerRecord.value())
    }


}