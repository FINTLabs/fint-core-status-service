package no.fintlabs.request;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class RequestFintEventEntity{

    @Id
    public String corrId;
    public String orgId;
    public String topic;
    public String domainName;
    public String packageName;
    public String resourceName;
    public String operationType;
    public long created;
    public long timeToLive;
    public String value;

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