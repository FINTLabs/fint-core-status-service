package no.novari.status.info

import no.novari.status.StatusProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Info(
    val enabled: Boolean,
)

@RestController
@RequestMapping("/api/v1/info")
class InfoController(
    private val properties: StatusProperties,
) {
    @GetMapping
    fun info(): Info = Info(properties.enabled)
}
