package no.fintlabs.sync

import no.fintlabs.sync.model.SyncEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SyncJpaRepository : JpaRepository<SyncEntity, Long> {

    fun findByOrgId(orgId: String): List<SyncEntity>

    fun findByCorrId(corrId: String): SyncEntity?

}