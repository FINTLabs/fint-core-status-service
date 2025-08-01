package no.fintlabs.consumerInfo

data class consumerInfo(
    var uid: String,
    var podName: String,
    var organisation: String,
    var restarts: String,
    var memoryUsage: String
)


fun convertBytesToMegabytes(value: String): String {
    return (value.toLong() / (1024 * 1024)).toString() + "MB"
}