package no.fintlabs.stats

data class Stats(
    val adapterContractAmount: Int,
    val hasContectAmount: Int,
    val eventAmount: Int,
    val eventErrors: Int,
    val eventResponses: Int,
)