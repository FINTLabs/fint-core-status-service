package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.AdapterHeartbeat;
import no.fintlabs.entities.AdapterContractEntity;
import no.fintlabs.kafka.common.topic.pattern.FormattedTopicComponentPattern;
import no.fintlabs.kafka.common.topic.pattern.ValidatedTopicComponentPattern;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Slf4j
@Service
public class AdapterHeartbeatService {

    private final EventConsumerFactoryService consumerFactory;
    private final AdapterContractRepository adapterContractRepository;

    public AdapterHeartbeatService(EventConsumerFactoryService consumerFactory, AdapterContractRepository adapterContractRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterContractRepository = adapterContractRepository;
    }

    @PostConstruct
    public void init() {


        consumerFactory.createFactory(
                AdapterHeartbeat.class,
                onAdapterHeartbeat(),
                new CommonLoggingErrorHandler(),
                false
        ).createContainer(
                EventTopicNamePatternParameters
                        .builder()
                        .orgId(FormattedTopicComponentPattern.any())
                        .domainContext(FormattedTopicComponentPattern.anyOf("fint-core"))
                        .eventName(ValidatedTopicComponentPattern.anyOf("adapter-health"))
                        .build()
        );
    }

    private Consumer<ConsumerRecord<String, AdapterHeartbeat>> onAdapterHeartbeat() {
        return (ConsumerRecord<String, AdapterHeartbeat> record) -> {
            AdapterHeartbeat adapterHeartbeat = record.value();
            log.trace(adapterHeartbeat.toString());
            AdapterContractEntity adapterContractEntity = adapterContractRepository.findAdapterContractByAdapterId(adapterHeartbeat.getAdapterId());
            adapterContractEntity.setLastSeen(adapterHeartbeat.getTime());
            adapterContractRepository.save(adapterContractEntity);
        };
    }
}
