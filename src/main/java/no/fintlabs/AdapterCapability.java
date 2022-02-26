package no.fintlabs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_capability")
public class AdapterCapability {
    @Id
    private String id;
    private String domain;
    @JsonProperty("package")
    private String packageName;
    @JsonProperty("class")
    private String clazz;
    private int fullSyncIntervalInDays;
    private DeltaSyncInterval deltaSyncInterval;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adapter_contract_adapter_id")
    @JsonIgnore
    private AdapterContract adapterContract;


    public String getEntityUri() {
        return String.format("/%s/%s/%s", domain, packageName, clazz);
    }

    public String getComponent() {
        return String.format("%s-%s", domain, packageName);
    }

    public enum DeltaSyncInterval {
        /**
         * This indicates that the adapter will send updates as soon as they are availiable in the application.
         */
        IMMEDIATE,
        /**
         * This indicates that the adapter will send updates every <=15 minutes.
         */
        LEGACY
    }
}