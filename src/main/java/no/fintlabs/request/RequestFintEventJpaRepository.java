package no.fintlabs.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestFintEventJpaRepository extends JpaRepository<RequestFintEventEntity, Long> {

    @Query("SELECT e FROM RequestFintEventEntity e WHERE e.created < :cutoffTimestamp")
    List<RequestFintEventEntity> findOlderThan(@Param("cutoffTimestamp") long cutoffTimestamp);

}
