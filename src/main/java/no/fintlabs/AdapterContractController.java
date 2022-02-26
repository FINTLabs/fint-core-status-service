package no.fintlabs;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/adapter/contract")
@RestController
public class AdapterContractController {

    private final AdapterContractRepository adapterContractRepository;

    public AdapterContractController(AdapterContractRepository adapterContractRepository) {
        this.adapterContractRepository = adapterContractRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdapterContract>> getAdapterContracts(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(adapterContractRepository.findAll());
    }

}
