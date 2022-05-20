package no.fintlabs;

import no.fintlabs.entities.AdapterContractEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/adapter/contract")
@RestController
public class AdapterContractController {

    private final AdapterContractRepository adapterContractRepository;

    public AdapterContractController(AdapterContractRepository adapterContractRepository) {
        this.adapterContractRepository = adapterContractRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdapterContractEntity>> getAdapterContracts() {
        List<AdapterContractEntity> adapterContractEntities = adapterContractRepository.findAll()
                .stream()
                .peek(adapterContractEntity -> {
                    Duration between = Duration.between(Instant.ofEpochMilli(adapterContractEntity.getLastSeen()), Instant.now());
                    adapterContractEntity.setConsiderHealthy(between.toMinutes() <= adapterContractEntity.getPingIntervalInMinutes());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(adapterContractEntities);
    }

}
