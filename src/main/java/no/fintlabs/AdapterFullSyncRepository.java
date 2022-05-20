package no.fintlabs;

import no.fintlabs.entities.AdapterFullSyncEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;

public interface AdapterFullSyncRepository extends JpaRepository<AdapterFullSyncEntity, String> {
    Flux<AdapterFullSyncEntity> findAdapterFullSyncEntitiesByOrgIdAndAdapterId(String orgId, String adapterId);

    Flux<AdapterFullSyncEntity> findAdapterFullSyncEntitiesByOrgIdAndAdapterIdAndCorrId(String orgId, String adapterId, String corrId);

    Flux<AdapterFullSyncEntity> findAdapterFullSyncEntitiesByOrgIdAndCorrId(String orgId, String corrId);
}
