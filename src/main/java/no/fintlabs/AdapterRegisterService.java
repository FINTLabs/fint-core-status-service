package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.AdapterContract;
import no.fintlabs.entities.AdapterCapabilityEntity;
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
import java.util.regex.Pattern;

@Slf4j
@Service
public class AdapterRegisterService {

    private final EventConsumerFactoryService consumerFactory;
    private final AdapterContractRepository adapterContractRepository;

    public AdapterRegisterService(EventConsumerFactoryService consumerFactory, AdapterContractRepository adapterContractRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterContractRepository = adapterContractRepository;
    }

    @PostConstruct
    public void init() {
        consumerFactory.createFactory(
                //Pattern.compile(".*.fint-core\\.event\\.adapter-register"),
                AdapterContract.class,
                onAdapterRegister(),
                new CommonLoggingErrorHandler(),
                false
        ).createContainer(
                EventTopicNamePatternParameters
                        .builder()
                        .orgId(FormattedTopicComponentPattern.any())
                        .domainContext(FormattedTopicComponentPattern.anyOf("fint-core"))
                        .eventName(ValidatedTopicComponentPattern.anyOf("adapter-register"))
                        .build()
        );
    }

    private Consumer<ConsumerRecord<String, AdapterContract>> onAdapterRegister() {
        return (ConsumerRecord<String, AdapterContract> record) -> {
            AdapterContract adapterContract = record.value();
            log.trace("Register adapter {}", adapterContract.toString());

            AdapterContractEntity adapterContractEntity = AdapterContractEntity.toEntity(adapterContract);
            adapterContractEntity.setLastSeen(System.currentTimeMillis());
            adapterContract.getCapabilities().forEach(adapterCapability -> adapterContractEntity
                    .getCapabilityEntities()
                    .add(AdapterCapabilityEntity.toEntity(adapterCapability, adapterContractEntity)));

            adapterContractRepository.save(adapterContractEntity);
        };
    }
}
