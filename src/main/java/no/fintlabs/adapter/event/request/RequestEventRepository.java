package no.fintlabs.adapter.event.request;

import no.fintlabs.adapter.entities.ConsumerRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestEventRepository extends JpaRepository<ConsumerRequestEntity, String> {

    public List<ConsumerRequestEntity> findRequestFintEventEntitiesByCorrId(String corrId);

}
