package no.fintlabs.adapter.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import no.fintlabs.adapter.models.SyncPageMetadata;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_delete_sync")
public class AdapterDeleteSyncEntity {

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

    public static AdapterDeleteSyncEntity toEntity(SyncPageMetadata syncPageMetadata) {
        return AdapterDeleteSyncEntity.builder()
                .adapterId(syncPageMetadata.getAdapterId())
                .corrId(syncPageMetadata.getCorrId())
                .totalSize(syncPageMetadata.getTotalSize())
                .page(syncPageMetadata.getPage())
                .pageSize(syncPageMetadata.getPageSize())
                .totalPages(syncPageMetadata.getTotalPages())
                .uriRef(syncPageMetadata.getUriRef())
                .time(syncPageMetadata.getTime())
                .build();
    }
}
