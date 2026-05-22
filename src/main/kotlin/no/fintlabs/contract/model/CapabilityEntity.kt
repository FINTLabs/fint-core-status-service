package no.fintlabs.contract.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import no.fintlabs.adapter.models.AdapterCapability
import java.time.Duration
import java.time.Instant

@Entity
@Table(name = "contract_capabilities")
class CapabilityEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "domain_name", nullable = false)
    var domainName: String = "",

    @Column(name = "package_name", nullable = false)
    var packageName: String = "",

    @Column(name = "resource_name", nullable = false)
    var resourceName: String = "",

    @Column(name = "component_name", nullable = false)
    var componentName: String = "",

    @Column(name = "entity_uri", nullable = false)
    var entityUri: String = "",

    @Column(name = "full_sync_interval_in_days", nullable = false)
    var fullSyncIntervalInDays: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "delta_sync_interval")
    var deltaSyncInterval: AdapterCapability.DeltaSyncInterval? = null,

    @Column(name = "follows_contract", nullable = false)
    var followsContract: Boolean = true,

    @Column(name = "last_full_sync")
    var lastFullSync: Long? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_adapter_id", nullable = false)
    var contract: ContractEntity? = null
) {

    fun updateLastFullSync(newLastFullSync: Long) {
        if (lastFullSync == null || newLastFullSync > lastFullSync!!) {
            lastFullSync = newLastFullSync
        }
    }

    fun updateFollowsContract() {
        followsContract = lastFullSync?.let {
            Duration.between(Instant.ofEpochMilli(it), Instant.now()).toDays()
        }?.let {
            it <= fullSyncIntervalInDays
        } ?: false
    }

    companion object {
        fun fromAdapterCapability(
            capability: AdapterCapability,
            contract: ContractEntity
        ): CapabilityEntity =
            CapabilityEntity(
                domainName = capability.domainName,
                packageName = capability.packageName,
                resourceName = capability.resourceName,
                componentName = capability.component,
                entityUri = capability.entityUri,
                fullSyncIntervalInDays = capability.fullSyncIntervalInDays,
                deltaSyncInterval = AdapterCapability.DeltaSyncInterval.IMMEDIATE,
                followsContract = true,
                lastFullSync = null,
                contract = contract
            )
    }
}