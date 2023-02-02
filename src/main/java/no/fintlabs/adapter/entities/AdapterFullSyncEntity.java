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
@Table(name = "adapter_full_sync")
public class AdapterFullSyncEntity {

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


    public static AdapterFullSyncEntity toEntity(SyncPageMetadata syncPageMetadata) {
        AdapterFullSyncEntity adapterFullSyncEntity = new AdapterFullSyncEntity();

        adapterFullSyncEntity.setAdapterId(syncPageMetadata.getAdapterId());
        adapterFullSyncEntity.setCorrId(syncPageMetadata.getCorrId());
        adapterFullSyncEntity.setOrgId(syncPageMetadata.getOrgId());
        adapterFullSyncEntity.setTotalSize(syncPageMetadata.getTotalSize());
        adapterFullSyncEntity.setPage(syncPageMetadata.getPage());
        adapterFullSyncEntity.setPageSize(syncPageMetadata.getPageSize());
        adapterFullSyncEntity.setTotalPages(syncPageMetadata.getTotalPages());
        adapterFullSyncEntity.setUriRef(syncPageMetadata.getUriRef());
        adapterFullSyncEntity.setTime(syncPageMetadata.getTime());

        return adapterFullSyncEntity;
    }
}
