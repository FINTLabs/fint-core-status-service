package no.fintlabs.contract

import no.fintlabs.contract.model.ContractEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContractJpaRepository: JpaRepository<ContractEntity, String> {

    fun getByOrgId(orgid: String): MutableList<ContractEntity>?
    fun findByorgId(orgId: String, pageable: Pageable): Page<ContractEntity>
    fun findByOrgIdAndCapabilitiesComponentName(orgId: String, componentName: String): List<ContractEntity>

}