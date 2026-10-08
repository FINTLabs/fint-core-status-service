package no.novari.status.web

class SqlWhere {
    private val clauses = mutableListOf<String>()
    val params = mutableMapOf<String, Any?>()

    fun add(
        clause: String,
        vararg values: Pair<String, Any?>,
    ): SqlWhere {
        clauses += clause
        params.putAll(values)
        return this
    }

    fun addIf(
        condition: Boolean,
        clause: String,
        vararg values: Pair<String, Any?>,
    ): SqlWhere = if (condition) add(clause, *values) else this

    fun sql(): String = if (clauses.isEmpty()) "" else "where " + clauses.joinToString(" and ")
}
