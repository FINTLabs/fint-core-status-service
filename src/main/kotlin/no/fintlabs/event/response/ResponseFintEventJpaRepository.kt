package no.fintlabs.event.response

import no.fintlabs.adapter.models.event.ResponseFintEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResponseFintEventJpaRepository: JpaRepository<ResponseFintEvent, Long> {
}