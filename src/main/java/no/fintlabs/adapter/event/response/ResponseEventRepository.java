package no.fintlabs.adapter.event.response;

import no.fintlabs.adapter.entities.ConsumerResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseEventRepository extends JpaRepository<ConsumerResponseEntity, String> {

    public List<ConsumerResponseEntity> findResponseFintEventEntitiesByCorrId(String corrId);

}
