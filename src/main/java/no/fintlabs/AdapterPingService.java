package no.fintlabs;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.kafka.event.FintKafkaEventConsumerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AdapterPingService {

    private final FintKafkaEventConsumerFactory consumerFactory;
    private final AdapterPingRepository adapterPingRepository;
    private final AdapterContractRepository adapterContractRepository;

    public AdapterPingService(FintKafkaEventConsumerFactory consumerFactory, AdapterPingRepository adapterPingRepository, AdapterContractRepository adapterContractRepository) {
        this.consumerFactory = consumerFactory;
        this.adapterPingRepository = adapterPingRepository;
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
            log.info(adapterPing.toString());
            AdapterContract adapterContract = adapterContractRepository.findAdapterContractByAdapterId(adapterPing.getAdapterId());
            adapterPing.setAdapterContract(adapterContract);
            adapterContract.setAdapterPing(adapterPing);
            adapterPingRepository.save(adapterPing);
            adapterContractRepository.save(adapterContract);
        };
    }

    private Consumer<JsonProcessingException> onJsonProcessingException() {
        return (JsonProcessingException jsonProcessingExeption) -> log.error(jsonProcessingExeption.getMessage());
    }

}
