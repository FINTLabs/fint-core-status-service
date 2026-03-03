package no.fintlabs.sync.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import no.fintlabs.adapter.models.sync.SyncType
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.transaction.annotation.Transactional

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


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var pages: MutableList<Page> = mutableListOf(),
) {
    protected constructor() : this(
        "", "", "", "", "", "",
        0L, 0L, 0L, 0L,
        SyncType.FULL, false, mutableListOf()
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

    fun addPage(sync: SyncEntity) {
        pages.add(sync.pages[0])
        pagesAcquired += 1
        entitiesAquired += sync.entitiesAquired
        finished = sync.totalPages == pagesAcquired
    }

}
