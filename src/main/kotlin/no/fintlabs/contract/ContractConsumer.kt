package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerConfiguration
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component


@Component
class ContractConsumer(val contractCache: ContractCache) {

    private val log = LoggerFactory.getLogger(ContractConsumer::class.java)

    @Bean
    fun registerAdapterContractKafkaConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, AdapterContract> {
        return eventConsumerFactoryService.createFactory(
            AdapterContract::class.java,
            this::processEvent,
            EventConsumerConfiguration.builder()
                .seekingOffsetResetOnAssignment(true)
                .build()
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.endingWith("adapter-register"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, AdapterContract>) {
        contractCache.add(consumerRecord.value())
    }

}