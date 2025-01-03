package no.fintlabs.event.request

import no.fintlabs.MappingService
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.event.cache.EventStatusCache
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerConfiguration
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import no.fintlabs.request.RequestFintEventJpaRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class RequestFintEventConsumer(
    val eventStatusCache: EventStatusCache,
    val requestFintEventJpaRepository: RequestFintEventJpaRepository,
    private val mappingService: MappingService
) {

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
        log.info("Consumed Request: {} from topic name: {}", consumerRecord.value().corrId, consumerRecord.topic())
        requestFintEventJpaRepository.save(mappingService.mapRequestFintEventToEntity(consumerRecord.value(), consumerRecord.topic()))
        eventStatusCache.add(consumerRecord.value(), consumerRecord.topic())
    }

}