package no.novari.status.org

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrgTreeTest {
    private val tree = OrgTree(listOf("oslo.kommune.no", "ude.oslo.kommune.no", "skole.ude.oslo.kommune.no", "afk.no"))

    @Test
    fun `a sub-org belongs to the top-most known org`() {
        assertEquals("oslo.kommune.no", tree.mainOf("ude.oslo.kommune.no"))
        assertEquals("oslo.kommune.no", tree.mainOf("skole.ude.oslo.kommune.no"))
        assertEquals("afk.no", tree.mainOf("afk.no"))
    }

    @Test
    fun `main orgs list their members with the main org first`() {
        assertEquals(listOf("afk.no", "oslo.kommune.no"), tree.mainOrgs())
        assertEquals(listOf("oslo.kommune.no", "skole.ude.oslo.kommune.no", "ude.oslo.kommune.no"), tree.membersOf("oslo.kommune.no"))
    }

    @Test
    fun `belongsTo matches the org itself and its sub-orgs only`() {
        assertTrue(OrgTree.belongsTo("ude.oslo.kommune.no", "oslo.kommune.no"))
        assertTrue(OrgTree.belongsTo("afk.no", "afk.no"))
        assertFalse(OrgTree.belongsTo("ofk.no", "fk.no"))
    }
}
