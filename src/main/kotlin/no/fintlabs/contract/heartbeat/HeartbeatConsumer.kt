package no.fintlabs.contract.heartbeat

import no.fintlabs.adapter.models.AdapterHeartbeat
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
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
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .domainContext(FormattedTopicComponentPattern.anyOf("fint-core"))
                .eventName(ValidatedTopicComponentPattern.endingWith("adapter-health"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, AdapterHeartbeat>) {
        heartbeatCache.add(consumerRecord.value())
    }


}