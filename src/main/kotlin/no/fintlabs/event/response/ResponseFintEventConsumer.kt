package no.fintlabs.event.response

import no.fintlabs.MappingService
import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.event.cache.EventStatusCache
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import no.fintlabs.response.ResponseFintEventJpaRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class ResponseFintEventConsumer(
    val eventStatusCache: EventStatusCache,
    val responseFintEventJpaRepository: ResponseFintEventJpaRepository,
    private val mappingService: MappingService
) {

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
        log.debug("Consumed Response: {}", consumerRecord.value().corrId)
        responseFintEventJpaRepository.save(mappingService.mapResponseFintEventToEntity(consumerRecord.value(), consumerRecord.topic()))
        eventStatusCache.add(consumerRecord.value(), consumerRecord.topic())
    }

}