package no.fintlabs.contract

import no.fintlabs.adapter.models.AdapterContract
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/adapter-contract")
class AdapterContractController(
    val adapterContractCache: AdapterContextCache
) {

    @GetMapping
    fun getContracts(): ResponseEntity<MutableCollection<AdapterContract>> {
        return ResponseEntity.ok(adapterContractCache.getAll())
    }

}