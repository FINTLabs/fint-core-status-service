package no.fintlabs.consumerInfo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/consumer")
class ConsumerController(private val consumerService: ConsumerService) {

    @GetMapping
    fun getConsumer() = consumerService.getCache()

}