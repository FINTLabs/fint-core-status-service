package no.fintlabs.page.kafka

import no.fintlabs.StatusTopicService
import no.fintlabs.kafka.event.EventProducer
import no.fintlabs.kafka.event.EventProducerFactory
import no.fintlabs.kafka.event.EventProducerRecord
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.page.model.PageMetadata
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PageProducer(
    facotry: EventProducerFactory,
    private val statusTopicService: StatusTopicService,
) {
    private val pageEventProducer: EventProducer<ResourceEvictionPayload> =
        facotry.createProducer(ResourceEvictionPayload::class.java)

    fun sendPage(page: PageMetadata) {
        val topicName = EventTopicNameParameters.builder()
            .orgId(page.orgId)
            .eventName("completed-full-sync")
            .domainContext("fint-core")
            .build()

        statusTopicService.ensureTopic(topicName, Duration.ofDays(7).toMillis())

        pageEventProducer.send(
            EventProducerRecord.builder<ResourceEvictionPayload>()
                .key(page.corrId)
                .value(ResourceEvictionPayload(page.domain, page.`package`, page.resource))
                .topicNameParameters(topicName)
                .build()
        )
    }
}