package no.fintlabs.adapter.datasync.deltaSync;

import no.fintlabs.adapter.entities.AdapterDeltaSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdapterDeltaSyncRepository extends JpaRepository<AdapterDeltaSyncEntity, String> {
}
