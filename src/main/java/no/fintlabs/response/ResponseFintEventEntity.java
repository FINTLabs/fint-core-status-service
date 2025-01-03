package no.fintlabs.response;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class ResponseFintEventEntity {

    @Id
    public String corrId;
    public String orgId;
    public String adapterId;
    public String topic;
    public long handledAt;
    public boolean failed;
    public String errorMessage;
    public String rejectReason;
    public boolean conflicted;
    public boolean rejected;
    public String conflictReason;
    public String syncPageEntryIdentifier;
    public String syncPageEntryResource;

    public ResponseFintEventEntity(String corrId, String orgId, String adapterId, String topic, long handledAt, boolean failed, String errorMessage, String rejectReason, boolean conflicted, boolean rejected, String conflictReason, String syncPageEntryIdentifier, String syncPageEntryResource) {
        this.corrId = corrId;
        this.orgId = orgId;
        this.adapterId = adapterId;
        this.topic = topic;
        this.handledAt = handledAt;
        this.failed = failed;
        this.errorMessage = errorMessage;
        this.rejectReason = rejectReason;
        this.conflicted = conflicted;
        this.rejected = rejected;
        this.conflictReason = conflictReason;
        this.syncPageEntryIdentifier = syncPageEntryIdentifier;
        this.syncPageEntryResource = syncPageEntryResource;
    }
}