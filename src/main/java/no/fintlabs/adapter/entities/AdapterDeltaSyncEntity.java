package no.fintlabs.adapter.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.fintlabs.adapter.models.SyncPageMetadata;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_delta_sync")

public class AdapterDeltaSyncEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    private String adapterId;

    private String corrId;
    private String orgId;
    private long totalSize;
    private long page;
    private long pageSize;
    private long totalPages;
    private String uriRef;
    private long time;

    public static AdapterDeltaSyncEntity toEntity(SyncPageMetadata syncPageMetadata) {
        AdapterDeltaSyncEntity adapterDeltaSyncEntity = new AdapterDeltaSyncEntity();

            adapterDeltaSyncEntity.setAdapterId(syncPageMetadata.getAdapterId());
            adapterDeltaSyncEntity.setCorrId(syncPageMetadata.getCorrId());
            adapterDeltaSyncEntity.setOrgId(syncPageMetadata.getOrgId());
            adapterDeltaSyncEntity.setTotalSize(syncPageMetadata.getTotalSize());
            adapterDeltaSyncEntity.setPage(syncPageMetadata.getPage());
            adapterDeltaSyncEntity.setTotalPages(syncPageMetadata.getTotalPages());
            adapterDeltaSyncEntity.setUriRef(syncPageMetadata.getUriRef());
            adapterDeltaSyncEntity.setTime(syncPageMetadata.getTime());

        return adapterDeltaSyncEntity;
    }

}
