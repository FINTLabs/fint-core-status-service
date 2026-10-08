package no.novari.status.kafka

/**
 * The key fint-core puts on contract records: the username and the orgId, separated by the
 * unit separator character (U+001F).
 */
data class ContractKey(
    val username: String,
    val orgId: String,
) {
    companion object {
        private const val SEPARATOR = '\u001F'

        fun parse(key: String?): ContractKey? {
            val parts = key?.split(SEPARATOR) ?: return null
            return if (parts.size == 2 && parts.none { it.isBlank() }) ContractKey(parts[0], parts[1]) else null
        }
    }
}
