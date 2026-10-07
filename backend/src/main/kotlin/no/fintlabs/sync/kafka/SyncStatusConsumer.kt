package no.fintlabs.sync.kafka

import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import no.fintlabs.sync.SyncStatusCache
import no.fintlabs.sync.model.SyncStatus
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class SyncStatusConsumer(
    private val syncStatusCache: SyncStatusCache
) {

    private val log = LoggerFactory.getLogger(SyncPageMetadataConsumer::class.java)

    @Bean
    fun registerSyncStatusConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, SyncStatus> {
        return eventConsumerFactoryService.createFactory(
            SyncStatus::class.java,
            this::processEvent
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.containing("fintlabs-no"))
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.containing("sync-status"))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, SyncStatus>) {
        syncStatusCache.addSyncStatus(consumerRecord.value())
    }
}