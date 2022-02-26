package no.fintlabs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_ping")
public class AdapterPing {
    @Id
    private String adapterId;
    private String username;
    private String orgId;
    private long time;
    @OneToOne(mappedBy = "adapterPing")
    @JsonIgnore
    private AdapterContract adapterContract;

}
