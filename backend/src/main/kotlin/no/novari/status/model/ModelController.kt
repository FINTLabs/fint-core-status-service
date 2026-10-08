package no.novari.status.model

import no.novari.fint.core.model.FintModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class ModelPackage(
    val name: String,
    val resources: List<String>,
)

data class ModelDomain(
    val name: String,
    val packages: List<ModelPackage>,
)

@RestController
@RequestMapping("/api/v1/model")
class ModelController {
    private val domains: List<ModelDomain> =
        FintModel.refs
            .groupBy { it.domainName.lowercase() }
            .toSortedMap()
            .map { (domain, refs) ->
                ModelDomain(
                    domain,
                    refs
                        .groupBy { it.packageName.lowercase() }
                        .toSortedMap()
                        .map { (pkg, inPackage) -> ModelPackage(pkg, inPackage.map { it.resourceName.lowercase() }.distinct().sorted()) },
                )
            }

    @GetMapping
    fun model(): List<ModelDomain> = domains
}
