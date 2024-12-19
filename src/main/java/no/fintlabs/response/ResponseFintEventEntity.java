package no.fintlabs.response;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
public class ResponseFintEventEntity {

    @Id
    public final String corrId;
    public final String orgId;
    public final String adapterId;
    public final String topic;
    public final long handledAt;
    public final boolean failed;
    public final String errorMessage;
    public final String rejectReason;
    public final boolean conflicted;
    public final boolean rejected;
    public final String conflictReason;
    public final String syncPageEntryIdentifier;
    public final String syncPageEntryResource;

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