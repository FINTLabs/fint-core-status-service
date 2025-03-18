package no.fintlabs.sync.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private fun createSyncPageMetadata(
    adapterId: String,
    corrId: String,
    orgId: String,
    uriRef: String?,
    totalSize: Long,
    page: Long,
    pageSize: Long,
    totalPages: Long,
    time: Long = 0L
): SyncPageMetadata {
    return SyncPageMetadata.builder()
        .adapterId(adapterId)
        .corrId(corrId)
        .orgId(orgId)
        .totalSize(totalSize)
        .page(page)
        .pageSize(pageSize)
        .totalPages(totalPages)
        .uriRef(uriRef)
        .time(time)
        .build()
}

class SyncMetadataTest {

    @Test
    fun `create SyncMetadata successfully`() {
        val metadata = createSyncPageMetadata(
            adapterId = "adapter-1",
            corrId = "123",
            orgId = "fintlabs-no",
            uriRef = "utdanning/vurdering/elevfravar",
            totalSize = 100,
            page = 1,
            pageSize = 30,
            totalPages = 3
        )

        val syncMetadata = SyncMetadata.create(metadata, "full")
        val (domain, pkg, resource) = metadata.uriRef.split("/")

        assertEquals(metadata.adapterId, syncMetadata.adapterId)
        assertEquals(metadata.corrId, syncMetadata.corrId)
        assertEquals(metadata.orgId, syncMetadata.orgId)
        assertEquals(domain, "utdanning")
        assertEquals(domain, syncMetadata.domain)
        assertEquals(pkg, "vurdering")
        assertEquals(pkg, syncMetadata.`package`)
        assertEquals(resource, "elevfravar")
        assertEquals(resource, syncMetadata.resource)
        assertEquals(metadata.totalSize, syncMetadata.totalEntities)
        assertEquals(metadata.totalPages, syncMetadata.totalPages)
        assertEquals(metadata.pageSize, syncMetadata.entitiesAquired)
        assertEquals(1, syncMetadata.pagesAcquired)
        assertEquals(metadata.page, syncMetadata.pages.get(0).pageNumber)
        assertEquals(metadata.time, syncMetadata.pages.get(0).time)
        assertEquals(metadata.pageSize, syncMetadata.pages.get(0).pageSize)
        assertFalse(syncMetadata.finished)
    }

    @Test
    fun `update SyncMetadata successfully`() {
        val amountOfPages = 4L
        val expectedTotalSize = 100L
        val metadata = createSyncPageMetadata(
            adapterId = "adapter-1",
            corrId = "123",
            orgId = "fintlabs-no",
            uriRef = "utdanning/vurdering/elevfravar",
            totalSize = expectedTotalSize,
            page = 1,
            pageSize = 25,
            totalPages = amountOfPages
        )

        val syncMetadata = SyncMetadata.create(metadata, "full")

        for (i in 2..amountOfPages) {
            val newMetadata = createSyncPageMetadata(
                adapterId = "adapter-1",
                corrId = "123",
                orgId = "fintlabs-no",
                uriRef = "utdanning/vurdering/elevfravar",
                totalSize = expectedTotalSize,
                page = i,
                pageSize = 25,
                totalPages = amountOfPages
            )
            syncMetadata.addPage(newMetadata)
        }

        assertEquals(amountOfPages, syncMetadata.totalPages)
        assertEquals(amountOfPages, syncMetadata.pagesAcquired)
        assertEquals(amountOfPages, syncMetadata.pages.size.toLong())
        assertEquals(expectedTotalSize, syncMetadata.entitiesAquired)

        for (i in 1..4) {
            assertEquals(i.toLong(), syncMetadata.pages.get(i - 1).pageNumber)
        }

        assertTrue(syncMetadata.finished)
    }

    @Test
    fun `update unfinished SyncMetadata`() {
        val amountOfPages = 4L
        val expectedTotalSize = 100L
        val actualTotalSize = 75L
        val pageSize = 25L
        val metadata = createSyncPageMetadata(
            adapterId = "adapter-1",
            corrId = "123",
            orgId = "fintlabs-no",
            uriRef = "utdanning/vurdering/elevfravar",
            totalSize = expectedTotalSize,
            page = 1,
            pageSize = 25,
            totalPages = amountOfPages
        )

        val syncMetadata = SyncMetadata.create(metadata, "full")

        for (i in 2..<amountOfPages) {
            val newMetadata = createSyncPageMetadata(
                adapterId = "adapter-1",
                corrId = "123",
                orgId = "fintlabs-no",
                uriRef = "utdanning/vurdering/elevfravar",
                totalSize = expectedTotalSize,
                page = i,
                pageSize = pageSize,
                totalPages = amountOfPages
            )
            syncMetadata.addPage(newMetadata)
        }

        assertEquals(amountOfPages, syncMetadata.totalPages)
        assertNotEquals(expectedTotalSize, syncMetadata.entitiesAquired)
        assertEquals(actualTotalSize, syncMetadata.entitiesAquired)
        assertNotEquals(amountOfPages, syncMetadata.pages.size.toLong())
        assertFalse(syncMetadata.finished)
    }

    @Test
    fun `create SyncMetadata formats correct uriRef`() {
        val domain = "utdanning"
        val `package` = "vurdering"
        val resource = "elevfravar"

        val metadata = createSyncPageMetadata(
            adapterId = "adapter-1",
            corrId = "123",
            orgId = "fintlabs-no",
            uriRef = "$domain/$`package`/$resource",
            totalSize = 1,
            page = 1,
            pageSize = 1,
            totalPages = 1
        )

        val syncMetadata = SyncMetadata.create(metadata, "full")

        assertEquals(domain, syncMetadata.domain)
        assertEquals(`package`, syncMetadata.`package`)
        assertEquals(resource, syncMetadata.resource)
    }

}
