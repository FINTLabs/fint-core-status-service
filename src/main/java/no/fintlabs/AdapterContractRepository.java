package no.fintlabs;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdapterContractRepository extends JpaRepository<AdapterContract, String> {
    AdapterContract findAdapterContractByAdapterId(String adapterId);
}
