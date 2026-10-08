package no.novari.status.org

/**
 * Groups orgs under their main org. An org belongs to another known org when its name ends
 * with "." plus that org, for example ude.oslo.kommune.no under oslo.kommune.no. The main
 * org is the top-most known org it belongs to.
 */
class OrgTree(
    orgIds: Collection<String>,
) {
    private val known = orgIds.toSortedSet()

    fun mainOf(orgId: String): String = known.filter { it != orgId && orgId.endsWith(".$it") }.minByOrNull { it.length } ?: orgId

    fun mainOrgs(): List<String> = known.map(::mainOf).distinct().sorted()

    fun membersOf(mainOrgId: String): List<String> = listOf(mainOrgId) + known.filter { it != mainOrgId && mainOf(it) == mainOrgId }

    companion object {
        fun belongsTo(
            orgId: String,
            scope: String,
        ): Boolean = orgId == scope || orgId.endsWith(".$scope")
    }
}
