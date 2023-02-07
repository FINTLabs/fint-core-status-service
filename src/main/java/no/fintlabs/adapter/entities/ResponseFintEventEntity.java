package no.fintlabs.adapter.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import no.fintlabs.adapter.models.ResponseFintEvent;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "adapter_event_response")
public class ResponseFintEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    private String corrId;
    private String orgId;
    private String adapterId;
    private long handledAt;
    private boolean failed;
    private String errorMessage;
    private boolean rejected;
    private String rejectReason;

    public static ResponseFintEventEntity toEntity(ResponseFintEvent responseFintEvent) {
        return ResponseFintEventEntity.builder()
                .corrId(responseFintEvent.getCorrId())
                .orgId(responseFintEvent.getOrgId())
                .adapterId(responseFintEvent.getAdapterId())
                .handledAt(responseFintEvent.getHandledAt())
                .failed(responseFintEvent.isFailed())
                .errorMessage(responseFintEvent.getErrorMessage())
                .rejected(responseFintEvent.isRejected())
                .rejectReason(responseFintEvent.getRejectReason())
                .build();
    }

}
