package no.fintlabs.page

import no.fintlabs.adapter.models.sync.SyncPageMetadata
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
class SyncPageMetadataConsumer(val pageMetadataCache: PageMetadataCache) {

    private val log = LoggerFactory.getLogger(SyncPageMetadataConsumer::class.java)

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
        val syncType = parts.getOrNull(parts.size - 2)
        log.debug("Consumed {}-sync From: {}", syncType, consumerRecord.value().adapterId)
        syncType?.let {
            pageMetadataCache.add(consumerRecord.value(), it)
        }
    }

}