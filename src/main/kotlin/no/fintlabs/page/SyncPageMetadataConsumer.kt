package no.fintlabs.page

import no.fintlabs.adapter.models.sync.SyncPageMetadata
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
    fun registerSyncPageConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, SyncPageMetadata> {
        return eventConsumerFactoryService.createFactory(
            SyncPageMetadata::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.any())
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.endingWith("-sync"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, SyncPageMetadata>) {
        val parts = consumerRecord.topic().split("-")
        val secondLastPart = parts.getOrNull(parts.size - 2)
        secondLastPart?.let {
            pageMetadataCache.add(consumerRecord.value(), it)
        }
    }

}