package no.fintlabs.contract

import no.fintlabs.contract.model.Contract
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contract")
class ContractController(
    val adapterContractCache: ContractCache,
) {

    @GetMapping
    fun getContracts(): ResponseEntity<MutableCollection<Contract>> = ResponseEntity.ok(adapterContractCache.getAll())

    @GetMapping("/{orgId}/{component}")
    fun getByOrgAndComponent(@PathVariable orgId: String, @PathVariable component: String): ResponseEntity<MutableList<Contract>> {
        return ResponseEntity.ok(adapterContractCache.getByOrgAndComponent(orgId, component))
    }
}