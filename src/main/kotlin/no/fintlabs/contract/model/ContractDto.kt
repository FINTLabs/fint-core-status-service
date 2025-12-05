package no.fintlabs.contract.model

data class ContractDto(
    val adapterId: String,
    val heartbeat: Boolean,
    val lastDelta: Long?,
    val lastFull: Long?
) {

}