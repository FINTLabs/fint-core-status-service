package no.fintlabs;

import no.fintlabs.entities.AdapterContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdapterContractRepository extends JpaRepository<AdapterContractEntity, String> {
    AdapterContractEntity findAdapterContractByAdapterId(String id);
}
