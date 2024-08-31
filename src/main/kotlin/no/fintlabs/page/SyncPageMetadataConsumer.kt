package no.fintlabs.page

import no.fintlabs.adapter.models.sync.SyncPage
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class SyncPageMetadataConsumer {

    @Bean
    fun registerSyncPageConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, SyncPage> {
        return eventConsumerFactoryService.createFactory(
            SyncPage::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .eventName(ValidatedTopicComponentPattern.endingWith("-sync"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, SyncPage>) {

    }

}