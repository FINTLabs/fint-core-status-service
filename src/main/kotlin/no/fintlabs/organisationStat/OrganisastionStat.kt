package no.fintlabs.model

class OrganisastionStat(
     val applications: Int = 0,
     val restarts: Int = 0,
     val consumerErrors: MutableMap<String, List<String>> = mutableMapOf()
)
