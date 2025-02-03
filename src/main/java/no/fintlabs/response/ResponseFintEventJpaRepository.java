package no.fintlabs.response;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseFintEventJpaRepository extends JpaRepository<ResponseFintEventEntity, Long> {

    @Query("SELECT e FROM ResponseFintEventEntity e WHERE e.handledAt < :cutoffTimestamp")
    List<ResponseFintEventEntity> findOlderThan(@Param("cutoffTimestamp") long cutoffTimestamp);

}