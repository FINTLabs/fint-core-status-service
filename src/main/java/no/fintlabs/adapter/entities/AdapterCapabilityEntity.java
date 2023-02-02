package no.fintlabs.adapter.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.fintlabs.adapter.models.AdapterCapability;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_capability")
public class AdapterCapabilityEntity {
    @Id
    @JsonIgnore
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adapter_contract_adapter_id")
    @JsonIgnore
    private AdapterContractEntity adapterContractEntity;

    private String domainName;
    private String packageName;
    private String resourceName;
    private int fullSyncIntervalInDays;
    private AdapterCapability.DeltaSyncInterval deltaSyncInterval;

    public static AdapterCapabilityEntity toEntity(AdapterCapability adapterCapability, AdapterContractEntity adapterContract) {
        AdapterCapabilityEntity adapterCapabilityEntity = new AdapterCapabilityEntity();

        adapterCapabilityEntity.setId(adapterContract.getAdapterId() + adapterCapability.getEntityUri());
        adapterCapabilityEntity.setDomainName(adapterCapability.getDomainName());
        adapterCapabilityEntity.setPackageName(adapterCapability.getPackageName());
        adapterCapabilityEntity.setResourceName(adapterCapability.getResourceName());
        adapterCapabilityEntity.setDeltaSyncInterval(adapterCapability.getDeltaSyncInterval());
        adapterCapabilityEntity.setFullSyncIntervalInDays(adapterCapability.getFullSyncIntervalInDays());
        adapterCapabilityEntity.setAdapterContractEntity(adapterContract);

        return adapterCapabilityEntity;
    }
}