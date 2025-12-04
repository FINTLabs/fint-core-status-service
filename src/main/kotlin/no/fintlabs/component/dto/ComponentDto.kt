package no.fintlabs.component.dto

data class ResourceDto(
    val resourceName: String,
    val heartbeat: Boolean,
    val followsContract: Boolean,
    val lastDelta: Long?,
    val lastFull: Long?
)

data class ComponentDto(
    val orgId: String,
    val component: String,
    val resources: List<ResourceDto>
)

data class ComponentsResponse(
    val components: List<ComponentDto>
)