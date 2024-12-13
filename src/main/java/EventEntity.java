import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class EventEntity {
    @Id
    private String corrId;
    private String topic;
    private String orgId;
    private boolean hasError;

//    @OneToOne
//    private RequestFintEventEntity requestFintEvent;
//
//    @OneToOne(optional = true)
//    @JoinColumn(name = "corrId")
//    private ResponseFintEventEntity responseFintEvent;

}
