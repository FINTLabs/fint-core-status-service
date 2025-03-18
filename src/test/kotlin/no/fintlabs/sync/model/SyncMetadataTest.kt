package no.fintlabs.sync.model

import no.fintlabs.adapter.models.sync.SyncPageMetadata
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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

    }
}
