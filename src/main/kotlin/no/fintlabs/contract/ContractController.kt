package no.fintlabs.contract

import no.fintlabs.contract.model.Contract
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contract")
class ContractController(
    val adapterContractCache: ContractCache,
    private val contractService: ContractService
) {

    @GetMapping
    fun getContracts(): ResponseEntity<MutableCollection<Contract>> = ResponseEntity.ok(adapterContractCache.getAll())

    @GetMapping("/inactive")
    fun getInactiveContracts(): ResponseEntity<List<Contract>> = ResponseEntity.ok(contractService.inactiveContracts())

}