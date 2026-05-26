package no.fintlabs.contract

import no.fintlabs.contract.model.ContractEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ContractJpaRepository : JpaRepository<ContractEntity, String> {

    fun getByOrgId(orgid: String): MutableList<ContractEntity>?

    @Query("SELECT c FROM ContractEntity c JOIN c.capabilities cap WHERE c.orgId = :orgId AND cap.componentName = :componentName")
    fun findByOrgIdAndCapabilitiesComponentName(orgId: String, componentName: String): List<ContractEntity>

}