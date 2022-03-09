package no.fintlabs;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.adapter.models.AdapterPing;
import no.fintlabs.entities.AdapterContractEntity;
import no.fintlabs.kafka.event.FintKafkaEventConsumerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AdapterPingService {

    private final FintKafkaEventConsumerFactory consumerFactory;
    private final AdapterContractRepository adapterContractRepository;

    public AdapterPingService(FintKafkaEventConsumerFactory consumerFactory, AdapterContractRepository adapterContractRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterContractRepository = adapterContractRepository;
    }

    @PostConstruct
    public void init() {
        consumerFactory.createConsumer(
                Pattern.compile(".*.fint-core\\.event\\.adapter-health"),
                AdapterPing.class,
                onAdapterPing(),
                onJsonProcessingException()
        );
    }

    private Consumer<AdapterPing> onAdapterPing() {
        return (AdapterPing adapterPing) -> {
            log.trace(adapterPing.toString());
            AdapterContractEntity adapterContractEntity = adapterContractRepository.findAdapterContractByAdapterId(adapterPing.getAdapterId());
            adapterContractEntity.setLastSeen(adapterPing.getTime());
            adapterContractRepository.save(adapterContractEntity);
        };
    }

    private Consumer<JsonProcessingException> onJsonProcessingException() {
        return (JsonProcessingException jsonProcessingException) -> log.error(jsonProcessingException.getMessage());
    }

}
