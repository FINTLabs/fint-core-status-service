package no.fintlabs.adapter.event.response;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.entities.ConsumerResponseEntity;
import no.fintlabs.adapter.models.ResponseFintEvent;
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern;
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern;
import no.fintlabs.kafka.event.EventConsumerConfiguration;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class ResponseEventTopicListener {

    private final ResponseEventRepository responseEventRepository;

    private final EventConsumerFactoryService consumerFactory;

    public ResponseEventTopicListener(EventConsumerFactoryService consumerFactory,
                                      ResponseEventRepository responseEventRepository) {
        this.consumerFactory = consumerFactory;
        this.responseEventRepository = responseEventRepository;
    }

    @PostConstruct
    private void init() {
        consumerFactory.createFactory(
                ResponseFintEvent.class,
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
                        .eventName(ValidatedTopicComponentPattern.endingWith("-response"))
                        .build()
        );
    }

    private void processEvent(ConsumerRecord<String, ResponseFintEvent> consumerRecord) {
        log.debug("ResponseFintEvent received: {} - {}",
                consumerRecord.value().getOrgId(),
                consumerRecord.value().getCorrId());

        ConsumerResponseEntity consumerResponseEntity = ConsumerResponseEntity.toEntity(consumerRecord.value());
        responseEventRepository.save(consumerResponseEntity);
    }

}
