package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.AdapterPing;
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
public class AdapterPingService {

    private final EventConsumerFactoryService consumerFactory;
    private final AdapterContractRepository adapterContractRepository;

    public AdapterPingService(EventConsumerFactoryService consumerFactory, AdapterContractRepository adapterContractRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterContractRepository = adapterContractRepository;
    }

    @PostConstruct
    public void init() {


        consumerFactory.createFactory(
                //Pattern.compile(".*.fint-core\\.event\\.adapter-health"),
                AdapterPing.class,
                onAdapterPing(),
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

    private Consumer<ConsumerRecord<String, AdapterPing>> onAdapterPing() {
        return (ConsumerRecord<String, AdapterPing> record) -> {
            AdapterPing adapterPing = record.value();
            log.trace(adapterPing.toString());
            AdapterContractEntity adapterContractEntity = adapterContractRepository.findAdapterContractByAdapterId(adapterPing.getAdapterId());
            adapterContractEntity.setLastSeen(adapterPing.getTime());
            adapterContractRepository.save(adapterContractEntity);
        };
    }
}
