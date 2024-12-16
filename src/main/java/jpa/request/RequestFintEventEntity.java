package jpa.request;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;

@Entity
@Builder
public class RequestFintEventEntity{

    @Id
    private String corrId;
    private String orgId;
    private String domainName;
    private String packageName;
    private String resourceName;
    private String operationType;
    private long created;
    private long timeToLive;
    private String value;
}