package no.fintlabs.event

import no.fintlabs.adapter.models.event.RequestFintEvent
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
class RequestFintEventConsumer(val eventStatusCache: EventStatusCache) {

    private val log = LoggerFactory.getLogger(RequestFintEventConsumer::class.java)

    @Bean
    fun registerRequestFintEventKafkaConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, RequestFintEvent> {
        return eventConsumerFactoryService.createFactory(
            RequestFintEvent::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.endingWith("-request"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, RequestFintEvent>) {
        log.info("Consumed Request: {}", consumerRecord.value().corrId)
        eventStatusCache.add(consumerRecord.value())
    }

}