package no.fintlabs.adapter.datasync.deletesync;

import no.fintlabs.adapter.entities.AdapterDeleteSyncEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/adapter/delete-sync")
@RestController
public class AdapterDeleteSyncController {

    private final AdapterDeleteSyncRepository adapterDeleteSyncRepository;

    public AdapterDeleteSyncController(AdapterDeleteSyncRepository adapterDeleteSyncRepository) {
        this.adapterDeleteSyncRepository = adapterDeleteSyncRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AdapterDeleteSyncEntity>> getAdapterDeleteSync(
            @RequestParam(value = "page", defaultValue = "0") int pageIndex,
            @RequestParam(value = "size", defaultValue = "50") int pageSize) {

        return ResponseEntity.ok(adapterDeleteSyncRepository.findAll(PageRequest.of(pageIndex, pageSize)));
    }

}
