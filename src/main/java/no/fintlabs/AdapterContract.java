package no.fintlabs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_contract")
public class AdapterContract implements Serializable {
    @Id
    private String adapterId;
    private String orgId;
    private String username;
    private long pingIntervalInMs;
    @OneToMany(mappedBy = "adapterContract", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AdapterCapability> capabilities = new LinkedHashSet<>();
    private long time;

    @OneToOne
    @JoinColumn(name = "adapter_ping_adapter_id")
    private AdapterPing adapterPing;
}
