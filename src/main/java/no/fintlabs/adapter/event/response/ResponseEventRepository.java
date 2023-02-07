package no.fintlabs.adapter.event.response;

import no.fintlabs.adapter.entities.ResponseFintEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseEventRepository extends JpaRepository<ResponseFintEventEntity, String> {

    public List<ResponseFintEventEntity> findResponseFintEventEntitiesByCorrId(String corrId);

}
