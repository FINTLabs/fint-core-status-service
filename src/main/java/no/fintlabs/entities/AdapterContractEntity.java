package no.fintlabs.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.fintlabs.adapter.models.AdapterContract;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_contract")
public class AdapterContractEntity {

    @Id
    private String adapterId;

    @OneToMany(mappedBy = "adapterContractEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AdapterCapabilityEntity> capabilityEntities = new LinkedHashSet<>();

    private String orgId;
    private String username;
    private int heartbeatIntervalInMinutes;
    private long time;
    private long lastSeen;
    @Transient
    private boolean isConsiderHealthy;

    public static AdapterContractEntity toEntity(AdapterContract adapterContract) {
        AdapterContractEntity adapterContractEntity = new AdapterContractEntity();

        adapterContractEntity.setAdapterId(adapterContract.getAdapterId());
        adapterContractEntity.setOrgId(adapterContract.getOrgId());
        adapterContractEntity.setHeartbeatIntervalInMinutes(adapterContract.getHeartbeatIntervalInMinutes());
        adapterContractEntity.setTime(adapterContract.getTime());
        adapterContractEntity.setUsername(adapterContract.getUsername());

        return adapterContractEntity;
    }
}
