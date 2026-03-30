package no.fintlabs.contract

import no.fintlabs.contract.model.AdapterStatus
import no.fintlabs.contract.model.Contract
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contract")
class ContractController(
    val adapterContractCache: ContractCache,
    val contractService: ContractService
) {

    @GetMapping
    fun getContracts(): ResponseEntity<MutableCollection<Contract>> {
        return ResponseEntity.ok(adapterContractCache.getAll())
    }

    @GetMapping("/inactive")
    fun getInactiveContracts(): List<Contract> = contractService.inactiveContracts()

    @GetMapping("/{orgId}/domain/{domain}")
    fun getDomainStatus(
        @PathVariable orgId: String,
        @PathVariable domain: String
    ) = contractService.getDomainForOrg(orgId, domain)

    @GetMapping("/{orgId}/component/{component}")
    fun getByOrgAndComponent(
        @PathVariable orgId: String,
        @PathVariable component: String
    ) = contractService.getByOrgAndComponent(orgId, component)

    @GetMapping("/status")
    fun getAdapters(): Set<AdapterStatus> = contractService.getStatus()
}