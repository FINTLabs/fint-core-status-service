package no.fintlabs.error.consumer

import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.organisationStat.OrganisatsionStatService
import no.fintlabs.status.models.error.ConsumerError
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class ConsumerErrorConsumer(
    private val organisatsionStatService: OrganisatsionStatService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun registerConsumerErrorConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, ConsumerError> {
        return eventConsumerFactoryService.createFactory(
            ConsumerError::class.java,
            this::processEvent
        ).createContainer(
            EventTopicNameParameters.builder()
                .orgId("fintlabs-no")
                .domainContext("fint-core")
                .eventName("consumer-error")
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, ConsumerError>) {
        val orgId = consumerRecord.topic().split(".")[0]
        val split = consumerRecord.topic().split("-")
        val domain = split[split.size - 2]
        val pkg = split[split.size - 1]

        organisatsionStatService.addError(orgId, "$domain $pkg", consumerRecord.value())
        logger.info("Consumed error: {}", consumerRecord.value())
    }
}