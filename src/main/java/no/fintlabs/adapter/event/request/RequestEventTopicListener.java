package no.fintlabs.adapter.event.request;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.entities.ConsumerRequestEntity;
import no.fintlabs.adapter.models.RequestFintEvent;
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern;
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern;
import no.fintlabs.kafka.event.EventConsumerConfiguration;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters;
import no.fintlabs.topic.TopicInfoService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class RequestEventTopicListener {

    private final TopicInfoService topicInfoService;

    private final RequestEventRepository requestEventRepository;

    private final EventConsumerFactoryService consumerFactory;

    public RequestEventTopicListener(TopicInfoService topicInfoService, EventConsumerFactoryService consumerFactory,
                                     RequestEventRepository requestEventRepository) {
        this.topicInfoService = topicInfoService;
        this.consumerFactory = consumerFactory;
        this.requestEventRepository = requestEventRepository;
    }

    @PostConstruct
    private void init() {
        consumerFactory.createFactory(
                RequestFintEvent.class,
                this::processEvent,
                EventConsumerConfiguration
                        .builder()
                        .seekingOffsetResetOnAssignment(false)
                        .build()
        ).createContainer(
                EventTopicNamePatternParameters
                        .builder()
                        .orgId(FormattedTopicComponentPattern.any())
                        .domainContext(FormattedTopicComponentPattern.anyOf("fint-core"))
                        .eventName(ValidatedTopicComponentPattern.endingWith("-request"))
                        .build()
        );
    }

    private void processEvent(ConsumerRecord<String, RequestFintEvent> consumerRecord) {
        topicInfoService.addToCache(consumerRecord.topic());
        log.debug("RequestFintEvent received: {} - {} from: {} - {} - {}",
                consumerRecord.value().getOrgId(),
                consumerRecord.value().getCorrId(),
                consumerRecord.value().getDomainName(),
                consumerRecord.value().getPackageName(),
                consumerRecord.value().getResourceName());

        ConsumerRequestEntity consumerRequestEntity = ConsumerRequestEntity.toEntity(consumerRecord.value());
        requestEventRepository.save(consumerRequestEntity);
    }

}
