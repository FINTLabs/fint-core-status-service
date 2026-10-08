package no.novari.status.web

data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Long,
)

data class CountResponse(
    val count: Long,
)

data class PageRequest(
    val page: Int,
    val size: Int,
) {
    val offset: Long get() = page.toLong() * size

    companion object {
        const val DEFAULT_SIZE = 25
        private const val MAX_SIZE = 100

        fun of(
            page: Int?,
            size: Int?,
        ) = PageRequest((page ?: 0).coerceAtLeast(0), (size ?: DEFAULT_SIZE).coerceIn(1, MAX_SIZE))
    }
}

enum class SortDirection { ASC, DESC }
