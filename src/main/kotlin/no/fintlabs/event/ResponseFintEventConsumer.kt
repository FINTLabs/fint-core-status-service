package no.fintlabs.event

import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class ResponseFintEventConsumer(val fintEventCache: FintEventCache) {

    private val log = LoggerFactory.getLogger(ResponseFintEventConsumer::class.java)

    @Bean
    fun registerResponseFintEventKafkaConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, ResponseFintEvent> {
        return eventConsumerFactoryService.createFactory(
            ResponseFintEvent::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.endingWith("-response"))
                .build()

        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, ResponseFintEvent>) {
        log.info("Consumed Response: {}", consumerRecord.value().corrId)
        fintEventCache.add(consumerRecord.value())
    }

}