package no.fintlabs.event.request

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RequestFintEventJpaRepository : JpaRepository<RequestFintEventEntity, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM RequestFintEventEntity e WHERE e.created < :cutoffTimestamp")
    fun deleteRowsOlderThan(@Param("cutoffTimestamp") cutoffTimestamp: Long): Int

}
