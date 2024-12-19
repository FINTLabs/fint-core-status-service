package no.fintlabs.request;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
public class RequestFintEventEntity{

    @Id
    public final String corrId;
    public final String orgId;
    public final String topic;
    public final String domainName;
    public final String packageName;
    public final String resourceName;
    public final String operationType;
    public final long created;
    public final long timeToLive;
    public final String value;

    public RequestFintEventEntity(String corrId, String orgId, String topic, String domainName, String packageName, String resourceName, String operationType, long created, long timeToLive, String value) {
        this.corrId = corrId;
        this.orgId = orgId;
        this.topic = topic;
        this.domainName = domainName;
        this.packageName = packageName;
        this.resourceName = resourceName;
        this.operationType = operationType;
        this.created = created;
        this.timeToLive = timeToLive;
        this.value = value;
    }
}