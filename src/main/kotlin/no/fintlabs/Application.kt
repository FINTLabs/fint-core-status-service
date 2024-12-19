package no.fintlabs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EntityScan(basePackages = ["no/fintlabs"])
class FintCoreStatusServiceApplication

fun main(args: Array<String>) {
    runApplication<FintCoreStatusServiceApplication>(*args)
}
