package no.novari.status.kafka

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContractKeyTest {
    @Test
    fun `a key from fint-core is split into username and orgId`() {
        assertEquals(ContractKey("adapter@afk.no", "afk.no"), ContractKey.parse("adapter@afk.no\u001Fafk.no"))
    }

    @Test
    fun `missing or malformed keys are rejected`() {
        assertNull(ContractKey.parse(null))
        assertNull(ContractKey.parse("adapter@afk.no"))
        assertNull(ContractKey.parse("\u001Fafk.no"))
        assertNull(ContractKey.parse("a\u001Fb\u001Fc"))
    }
}
