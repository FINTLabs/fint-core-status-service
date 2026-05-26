package no.fintlabs.stats

import no.fintlabs.contract.ContractJpaRepository
import no.fintlabs.contract.model.Contract
import no.fintlabs.contract.model.ContractEntity
import no.fintlabs.event.EventStatus
import no.fintlabs.event.FintEventService
import org.springframework.stereotype.Service

@Service
class StatsService(
    val fintEventService: FintEventService,
    val contractRepo: ContractJpaRepository
) {

    fun getStats(): Stats {
        val contracts = contractRepo.findAll()
        val events = fintEventService.getAllEvents()
        val adapterContractAmount = contracts.count()
        val hasContectAmount = getHasContactAmount(contracts)
        val eventAmount = events.count()
        val eventErrors = getEventErrorAmount(events)
        val eventResponses = getEventResponseCount(events)

        val stats = Stats(
            adapterContractAmount = adapterContractAmount,
            hasContectAmount = hasContectAmount,
            eventAmount = eventAmount,
            eventErrors = eventErrors,
            eventResponses = eventResponses
        )

        return stats
    }

    private fun getHasContactAmount(contracts: Collection<ContractEntity>): Int {
        return contracts.count { it.hasContact }
    }

    private fun getEventErrorAmount(events: Collection<EventStatus>): Int {
        return events.count { it.hasError }
    }

    private fun getEventResponseCount(events: Collection<EventStatus>): Int {
        return events.count { it.responseEvent != null }
    }

}