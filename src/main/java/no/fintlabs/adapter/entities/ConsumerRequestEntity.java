package no.fintlabs.adapter.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import no.fintlabs.adapter.models.OperationType;
import no.fintlabs.adapter.models.RequestFintEvent;

import javax.persistence.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "consumer_event_request")
public class ConsumerRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    private String corrId;
    private String orgId;
    private String domainName;
    private String packageName;
    private String resourceName;
    private OperationType operation;
    private long created;
    private long timeToLive;

    public static ConsumerRequestEntity toEntity(RequestFintEvent requestFintEvent) {
        return ConsumerRequestEntity.builder()
                .corrId(requestFintEvent.getCorrId())
                .orgId(requestFintEvent.getOrgId())
                .domainName(requestFintEvent.getDomainName())
                .packageName(requestFintEvent.getPackageName())
                .resourceName(requestFintEvent.getResourceName())
                .operation(requestFintEvent.getOperationType())
                .created(requestFintEvent.getCreated())
                .timeToLive(requestFintEvent.getTimeToLive())
                .build();
    }
}
