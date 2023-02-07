package no.fintlabs.adapter.event.request;

import no.fintlabs.adapter.entities.RequestFintEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestEventRepository extends JpaRepository<RequestFintEventEntity, String> {

    public List<RequestFintEventEntity> findRequestFintEventEntitiesByCorrId(String corrId);

}
