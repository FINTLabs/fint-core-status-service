package no.fintlabs.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Metric(
    @JsonProperty("__name__")
    val name: String,
    val endpoint: String,
    val instance: String,
    val pod: String,
    val namespace: String,
    val job: String,
    val container: String
)