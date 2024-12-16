package request;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RequestFintEventEntity {

    @Id
    private String corrId;
}