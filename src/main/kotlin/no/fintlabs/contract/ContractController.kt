package no.fintlabs.contract

import no.fintlabs.contract.model.Contract
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contract")
class ContractController(
    val adapterContractCache: ContractCache
) {

    @GetMapping
    fun getContracts(): ResponseEntity<MutableCollection<Contract>> = ResponseEntity.ok(adapterContractCache.getAll())

}