package no.fintlabs.event.request

import no.fintlabs.adapter.models.event.RequestFintEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RequestFintEventJpaRepository: JpaRepository<RequestFintEvent, Long> {
}