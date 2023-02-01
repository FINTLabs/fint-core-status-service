package no.fintlabs.adapter.contract;

import no.fintlabs.adapter.entities.AdapterContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdapterContractRepository extends JpaRepository<AdapterContractEntity, String> {
    AdapterContractEntity findAdapterContractByAdapterId(String id);
}
