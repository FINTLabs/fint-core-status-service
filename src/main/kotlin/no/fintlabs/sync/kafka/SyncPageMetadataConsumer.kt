package no.fintlabs.sync.kafka

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.contract.ContractCache
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern
import no.fintlabs.kafka.event.EventConsumerFactoryService
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters
import no.fintlabs.sync.SyncCache
import no.fintlabs.sync.kafka.KafkaTopicConstants.Companion.ADAPTER_SYNC_TOPICS
import no.fintlabs.sync.model.SyncMetadata
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.stereotype.Component

@Component
class SyncPageMetadataConsumer(
    val syncCache: SyncCache,
    val contractCache: ContractCache,
) {

    private val log = LoggerFactory.getLogger(SyncPageMetadataConsumer::class.java)

    @Bean
    fun registerSyncPageConsumer(eventConsumerFactoryService: EventConsumerFactoryService): ConcurrentMessageListenerContainer<String, SyncPageMetadata> {
        return eventConsumerFactoryService.createFactory(
            SyncPageMetadata::class.java,
            this::processEvent,
        ).createContainer(
            EventTopicNamePatternParameters.builder()
                .orgId(FormattedTopicComponentPattern.containing("fintlabs-no"))
                .domainContext(FormattedTopicComponentPattern.containing("fint-core"))
                .eventName(ValidatedTopicComponentPattern.anyOf(*ADAPTER_SYNC_TOPICS))
                .build()
        )
    }

    fun processEvent(consumerRecord: ConsumerRecord<String, SyncPageMetadata>) {
        val parts = consumerRecord.topic().split("-")
        val syncType = parts.getOrNull(parts.size - 2)
        val pageMetaData = consumerRecord.value()
        requireNotNull(syncType) { "Sync type is required" }

        log.debug("Consumed {}-sync From: {}", syncType, pageMetaData.adapterId)
        contractCache.updateLastActivity(pageMetaData.adapterId, pageMetaData.time)
        syncCache.add(SyncMetadata.create(pageMetaData, syncType))
    }
}