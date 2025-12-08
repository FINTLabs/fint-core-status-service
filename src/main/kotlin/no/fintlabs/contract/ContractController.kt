package no.fintlabs.contract

import no.fintlabs.contract.model.AdapterStatus
import no.fintlabs.contract.model.Contract
import no.fintlabs.contract.model.ContractDto
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
    fun getContracts(): ResponseEntity<MutableCollection<Contract>> = ResponseEntity.ok(adapterContractCache.getAll())

    @GetMapping("/{orgId}/{component}")
    fun getByOrgAndComponent(
        @PathVariable orgId: String,
        @PathVariable component: String
    ): ResponseEntity<MutableList<ContractDto>> {
        return ResponseEntity.ok(contractService.getByOrgAndComponent(orgId, component))
    }

    @GetMapping("/status")
    fun getAdapters(): List<AdapterStatus> = contractService.getStatus()
}