package no.novari.status.org

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class OrgNode(
    val orgId: String,
    val subOrgs: List<String>,
)

@RestController
@RequestMapping("/api/v1/orgs")
class OrgController(
    private val jdbcClient: JdbcClient,
) {
    @GetMapping
    fun orgs(): List<OrgNode> {
        val orgIds =
            jdbcClient
                .sql("select org_id from contract union select distinct org_id from sync union select distinct org_id from event")
                .query(String::class.java)
                .list()
        val tree = OrgTree(orgIds.filterNotNull())
        return tree.mainOrgs().map { main -> OrgNode(main, tree.membersOf(main).drop(1)) }
    }
}
