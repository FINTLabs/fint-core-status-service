package no.fintlabs

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FintCoreStatusServiceApplication

fun main(args: Array<String>) {
	runApplication<FintCoreStatusServiceApplication>(*args)
}
