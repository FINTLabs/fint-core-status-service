package no.fintlabs.event.request

import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.fintlabs.adapter.models.event.RequestFintEvent

@Entity
data class RequestFintEventEntity(
    @Id
    val corrId: String

) : RequestFintEvent()