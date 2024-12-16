package response;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ResponseFintEventEntity {

    @Id
    private String corrId;
}