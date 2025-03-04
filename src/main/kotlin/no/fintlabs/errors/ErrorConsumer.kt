package no.fintlabs.errors

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
class ErrorConsumer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun registerErrorKafkaConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, String> {
        return eventConsumerFactoryService.createFactory(
            String::class.java,
            this::processEvent
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.endingWith("-error"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, String>) {
        logger.info("Consumed error: {}", consumerRecord.value())
    }
}