package no.fintlabs.entities;

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

    private String domain;
    private String packageName;
    private String clazz;
    private int fullSyncIntervalInDays;
    private AdapterCapability.DeltaSyncInterval deltaSyncInterval;

    public static AdapterCapabilityEntity toEntity(AdapterCapability adapterCapability, AdapterContractEntity adapterContract) {
        AdapterCapabilityEntity adapterCapabilityEntity = new AdapterCapabilityEntity();

        adapterCapabilityEntity.setId(adapterContract.getAdapterId() + adapterCapability.getEntityUri());
        adapterCapabilityEntity.setDomain(adapterCapability.getDomain());
        adapterCapabilityEntity.setPackageName(adapterCapability.getPackageName());
        adapterCapabilityEntity.setClazz(adapterCapability.getClazz());
        adapterCapabilityEntity.setDeltaSyncInterval(adapterCapability.getDeltaSyncInterval());
        adapterCapabilityEntity.setFullSyncIntervalInDays(adapterCapability.getFullSyncIntervalInDays());
        adapterCapabilityEntity.setAdapterContractEntity(adapterContract);

        return adapterCapabilityEntity;
    }
}