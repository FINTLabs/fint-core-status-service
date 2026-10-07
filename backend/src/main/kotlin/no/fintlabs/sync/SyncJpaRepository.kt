package no.fintlabs.sync

import jakarta.transaction.Transactional
import no.fintlabs.sync.model.SyncEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SyncJpaRepository : JpaRepository<SyncEntity, Long> {

    fun findByOrgId(orgId: String): List<SyncEntity>

    fun findByCorrId(corrId: String): SyncEntity?

    @Modifying
    @Transactional
    @Query("DELETE FROM SyncEntity e WHERE e.savedAtTimeStamp < :cutoffTimestamp")
    fun deleteRowsOlderThan(@Param("cutoffTimestamp") cutoffTimestamp: Long): Int

    @Query("""
    SELECT s 
    FROM SyncEntity s
    WHERE s.savedAtTimeStamp BETWEEN :before AND :after
""")
    fun findByTime(
        @Param("before") before: Long,
        @Param("after") after: Long
    ): Collection<SyncEntity>

}