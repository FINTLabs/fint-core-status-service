package no.fintlabs.sync.kafka

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.contract.ContractService
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
    val contractService: ContractService,
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
        val topicSplit = consumerRecord.topic().split("-")
        val syncType = topicSplit[topicSplit.size - 2]
        val syncMetadata = SyncMetadata.create(consumerRecord.value(), syncType)

        log.debug("Consumed {}-sync From: {}", syncType, syncMetadata.adapterId)
        contractService.updateActivity(syncMetadata)
        syncCache.add(syncMetadata)
    }
}