package no.fintlabs.adapter.fullsync;

import no.fintlabs.adapter.entities.AdapterFullSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdapterFullSyncRepository extends JpaRepository<AdapterFullSyncEntity, String> {
    List<AdapterFullSyncEntity> findAdapterFullSyncEntitiesByOrgIdAndAdapterId(String orgId, String adapterId);

    List<AdapterFullSyncEntity> findAdapterFullSyncEntitiesByOrgIdAndAdapterIdAndCorrId(String orgId, String adapterId, String corrId);

    List<AdapterFullSyncEntity> findAdapterFullSyncEntitiesByOrgIdAndCorrId(String orgId, String corrId);
}
