package no.novari.status.contract

import no.novari.status.web.PageRequest
import no.novari.status.web.SortDirection
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/contracts")
class ContractController(
    private val contractService: ContractService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "ALL") filter: ContractFilter,
        @RequestParam(defaultValue = "HEALTH") sort: ContractSort,
        @RequestParam(defaultValue = "ASC") direction: SortDirection,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?,
    ): ContractPage = contractService.list(q, filter, sort, direction, PageRequest.of(page, size))

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
    ): ContractDetail = contractService.get(id)

    @PutMapping("/{id}/mute")
    fun mute(
        @PathVariable id: Long,
        @RequestBody(required = false) request: MuteRequest?,
        authentication: Authentication,
    ): ContractDetail = contractService.mute(id, request ?: MuteRequest(), authentication.name)

    @DeleteMapping("/{id}/mute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unmute(
        @PathVariable id: Long,
    ) = contractService.unmute(id)
}
