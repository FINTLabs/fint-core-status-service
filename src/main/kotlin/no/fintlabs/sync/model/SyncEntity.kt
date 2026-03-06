package no.fintlabs.sync.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.fintlabs.adapter.models.sync.SyncType
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
open class SyncEntity(

    @Id
    var corrId: String,
    var adapterId: String,
    var orgId: String,
    var domain: String,
    var `package`: String,
    var resource: String,
    var totalPages: Long,
    var pagesAcquired: Long,
    var totalEntities: Long,
    var entitiesAquired: Long,
    var syncType: SyncType,
    var finished: Boolean = false,
    val savedAtTimeStamp: Long = System.currentTimeMillis(),


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var pages: MutableList<Page> = mutableListOf(),
) {
    protected constructor() : this(
        "", "", "", "", "", "",
        0L, 0L, 0L, 0L,
        SyncType.FULL, false, System.currentTimeMillis(),mutableListOf()
    )

    open fun toDomain() = SyncMetadata(
        corrId = corrId,
        adapterId = adapterId,
        orgId = orgId,
        domain = domain,
        `package` = `package`,
        resource = resource,
        totalPages = totalPages,
        pagesAcquired = pagesAcquired,
        totalEntities = totalEntities,
        entitiesAquired = entitiesAquired,
        syncType = syncType,
        pages = pages.map { it }.toMutableList(),
        finished = finished
    )
}
