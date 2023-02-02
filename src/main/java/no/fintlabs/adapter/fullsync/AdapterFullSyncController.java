package no.fintlabs.adapter.fullsync;

import no.fintlabs.adapter.entities.AdapterFullSyncEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/adapter/full-sync")
@RestController
public class AdapterFullSyncController {

    private final AdapterFullSyncRepository adapterFullSyncRepository;

    public AdapterFullSyncController(AdapterFullSyncRepository adapterFullSyncRepository) {
        this.adapterFullSyncRepository = adapterFullSyncRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AdapterFullSyncEntity>> getAdapterContracts(
            @RequestParam(value = "page", defaultValue = "0") int pageIndex,
            @RequestParam(value = "size", defaultValue = "50") int pageSize) {

        return ResponseEntity.ok(adapterFullSyncRepository.findAll(PageRequest.of(pageIndex, pageSize)));
    }

}
