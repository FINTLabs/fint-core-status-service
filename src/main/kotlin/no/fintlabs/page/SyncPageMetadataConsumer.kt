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
class SyncPageMetadataConsumer(
    val pageMetadataCache: PageMetadataCache
) {

    @Bean
    fun registerSyncPageConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, SyncPage> {
        return eventConsumerFactoryService.createFactory(
            SyncPage::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.endingWith("-sync"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, SyncPage>) {
        pageMetadataCache.add(consumerRecord.value().metadata)
    }

}