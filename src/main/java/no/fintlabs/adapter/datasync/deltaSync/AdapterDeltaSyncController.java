package no.fintlabs.adapter.datasync.deltaSync;


import no.fintlabs.adapter.entities.AdapterDeltaSyncEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/adapter/delta-sync")
public class AdapterDeltaSyncController {

    private final AdapterDeltaSyncRepository adapterDeltaSyncRepository;

    public AdapterDeltaSyncController(AdapterDeltaSyncRepository adapterDeltaSyncRepository) {
        this.adapterDeltaSyncRepository = adapterDeltaSyncRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AdapterDeltaSyncEntity>> getAdapterDeltaSync(
            @RequestParam(value = "page", defaultValue = "0") int pageIndex,
            @RequestParam(value = "size", defaultValue = "50") int pageSize){

        return ResponseEntity.ok(adapterDeltaSyncRepository.findAll(PageRequest.of(pageIndex, pageSize)));
    }
}
