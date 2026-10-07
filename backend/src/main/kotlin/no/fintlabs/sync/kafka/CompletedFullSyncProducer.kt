package no.fintlabs.sync.kafka

import no.fintlabs.core.kafka.utils.CoreTopicService
import no.fintlabs.kafka.event.EventProducer
import no.fintlabs.kafka.event.EventProducerFactory
import no.fintlabs.kafka.event.EventProducerRecord
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.status.models.ResourceEvictionPayload
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service
class CompletedFullSyncProducer(
    facotry: EventProducerFactory,
    coreTopicService: CoreTopicService
) {

    private val eventTopicName = createEventTopicNameParameter()
    private val pageEventProducer: EventProducer<ResourceEvictionPayload> =
        facotry.createProducer(ResourceEvictionPayload::class.java)

    init {
        coreTopicService.ensureTopic(eventTopicName, Duration.ofDays(7).toMillis())
    }

    fun publishCompletedFullSync(page: SyncMetadata): CompletableFuture<SendResult<String, ResourceEvictionPayload>> =
        pageEventProducer.send(
            EventProducerRecord.builder<ResourceEvictionPayload>()
                .key(page.corrId)
                .value(
                    ResourceEvictionPayload(
                        page.domain,
                        page.`package`,
                        page.resource,
                        page.orgId,
                        System.currentTimeMillis()
                    )
                )
                .topicNameParameters(eventTopicName)
                .build()
        )

    private fun createEventTopicNameParameter() =
        EventTopicNameParameters.builder()
            .orgId("fintlabs-no")
            .eventName("completed-full-sync")
            .domainContext("fint-core")
            .build()

}