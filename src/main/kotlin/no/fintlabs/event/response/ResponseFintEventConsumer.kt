package no.fintlabs.event.response

import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.contract.ContractService
import no.fintlabs.event.cache.EventStatusCache
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.stereotype.Component

@Component
class ResponseFintEventConsumer(
    private val eventStatusCache: EventStatusCache,
    private val responseFintEventJpaRepository: ResponseFintEventJpaRepository,
    private val contractService: ContractService
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
        val responseEvent = consumerRecord.value()
        try {
            log.info("Consumed Response: {}", responseEvent.corrId)
            require(responseEvent.adapterId.isNotBlank()) {
                "adapterId is missing for corrId=${responseEvent.corrId}"
            }
            contractService.updateActivity(responseEvent.adapterId, responseEvent.handledAt)
            responseFintEventJpaRepository.save(mappingService.mapResponseFintEventToEntity(responseEvent, consumerRecord.topic()))
            eventStatusCache.add(responseEvent, consumerRecord.topic())
        } catch (e: ListenerExecutionFailedException) {
            log.error("AdapterId missing on event with corrId={}", responseEvent.corrId, e)
        }
    }

}