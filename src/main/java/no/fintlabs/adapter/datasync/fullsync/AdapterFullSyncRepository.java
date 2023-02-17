package no.fintlabs.adapter.datasync.fullsync;

import no.fintlabs.adapter.entities.AdapterFullSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdapterFullSyncRepository extends JpaRepository<AdapterFullSyncEntity, String> {
}
