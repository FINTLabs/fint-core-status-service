package no.fintlabs.entities

import no.fintlabs.adapter.models.AdapterContract
import spock.lang.Specification

class AdapterContractEntitySpec extends Specification {

    def "Create should make a entity from POJO"() {
        given:
        def contract = AdapterContract.builder()
                .username("test@adapter.test.com")
                .orgId("test.com")
                .adapterId("https://test.com/test.com/test/test")
                .pingIntervalInMinutes(10)
                .time(System.currentTimeMillis())
                .capabilities(Collections.emptySet())
                .build()

        when:
        def contractEntity = AdapterContractEntity.toEntity(contract)

        then:
        contractEntity.adapterId == "https://test.com/test.com/test/test"
    }
}

