package no.fintlabs.response;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseFintEventEntity {

    @Id
    private String corrId;
    private String orgId;
    private String adapterId;
    private long handledAt;
    private boolean failed;
    private String errorMessage;
    private boolean rejected;
    private String rejectReason;
    private boolean conflicted;
    private String conflictReason;
    private String syncPageEntryIdentifier;
    private String syncPageEntryResource;

}