package no.fintlabs.consumerInfo

data class Response(
    val data: PrometheusData
)

data class PrometheusData(
    val result: List<PrometheusResult>
)

data class PrometheusResult(
    val metric: PrometheusMetric,
    val value: List<String>
)

data class PrometheusMetric(
    val pod: String,
    val namespace: String,
    val uid: String
)

data class RestartsInfo(
    val status: String,
    val data: RestartPrometheusData
)

data class RestartPrometheusData(
    val resultType: String,
    val result: List<RetartPrometheusResult>
)

data class RetartPrometheusResult(
    val metric: PrometheusMetric,
    val value: List<String>
)
