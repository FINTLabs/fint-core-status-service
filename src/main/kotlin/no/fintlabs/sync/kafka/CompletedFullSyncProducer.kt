package no.fintlabs.sync.kafka

import no.fintlabs.StatusTopicService
import no.fintlabs.adapter.models.event.ResourceEvictionPayload
import no.fintlabs.kafka.event.EventProducer
import no.fintlabs.kafka.event.EventProducerFactory
import no.fintlabs.kafka.event.EventProducerRecord
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.status.models.ResourceEvictionPayload
import no.fintlabs.sync.model.SyncMetadata
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CompletedFullSyncProducer(
    facotry: EventProducerFactory,
    private val statusTopicService: StatusTopicService,
) {
    private val pageEventProducer: EventProducer<ResourceEvictionPayload> =
        facotry.createProducer(ResourceEvictionPayload::class.java)

    fun publishCompletedFullSync(page: SyncMetadata) {
        val topicName = createEventTopicNameParameter()
        statusTopicService.ensureTopic(topicName, Duration.ofDays(7).toMillis())
        pageEventProducer.send(
            EventProducerRecord.builder<ResourceEvictionPayload>()
                .key(page.corrId)
                .value(ResourceEvictionPayload(page.domain, page.`package`, page.resource, page.orgId, System.currentTimeMillis()))
                .topicNameParameters(topicName)
                .build()
        )
    }

    private fun createEventTopicNameParameter() =
        EventTopicNameParameters.builder()
            .orgId("fintlabs-no")
            .eventName("completed-full-sync")
            .domainContext("fint-core")
            .build()

}