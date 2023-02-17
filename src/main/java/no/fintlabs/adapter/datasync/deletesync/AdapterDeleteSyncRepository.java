package no.fintlabs.adapter.datasync.deletesync;

import no.fintlabs.adapter.entities.AdapterDeleteSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdapterDeleteSyncRepository extends JpaRepository<AdapterDeleteSyncEntity, String> {
}
