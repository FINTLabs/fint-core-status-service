package no.fintlabs;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.AdapterContract;
import no.fintlabs.entities.AdapterCapabilityEntity;
import no.fintlabs.entities.AdapterContractEntity;
import no.fintlabs.kafka.event.FintKafkaEventConsumerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AdapterRegisterService {

    private final FintKafkaEventConsumerFactory consumerFactory;
    private final AdapterContractRepository adapterContractRepository;

    public AdapterRegisterService(FintKafkaEventConsumerFactory consumerFactory, AdapterContractRepository adapterContractRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterContractRepository = adapterContractRepository;
    }

    @PostConstruct
    public void init() {
        consumerFactory.createConsumer(
                Pattern.compile(".*.fint-core\\.event\\.adapter-register"),
                AdapterContract.class,
                onAdapterRegister(),
                onJsonProcessingException()
        );
    }

    private Consumer<AdapterContract> onAdapterRegister() {
        return (AdapterContract adapterContract) -> {
            log.trace("Register adapter {}", adapterContract.toString());

            AdapterContractEntity adapterContractEntity = AdapterContractEntity.toEntity(adapterContract);
            adapterContract.getCapabilities().forEach(adapterCapability -> adapterContractEntity
                    .getCapabilityEntities()
                    .add(AdapterCapabilityEntity.toEntity(adapterCapability, adapterContractEntity)));

            adapterContractRepository.save(adapterContractEntity);
        };
    }

    private Consumer<JsonProcessingException> onJsonProcessingException() {
        return (JsonProcessingException jsonProcessingException) -> log.error(jsonProcessingException.getMessage());
    }

}
