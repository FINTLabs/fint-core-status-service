package no.fintlabs.response;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseFintEventJpaRepository extends JpaRepository<ResponseFintEventEntity, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ResponseFintEventEntity e WHERE e.handledAt < :cutoffTimestamp")
    int deleteRowsOlderThan(@Param("cutoffTimestamp") long cutoffTimestamp);
}