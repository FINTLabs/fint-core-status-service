package no.fintlabs.contract.model

import jakarta.persistence.*
import no.fintlabs.adapter.models.AdapterContract

@Entity
@Table(name = "contracts")
class ContractEntity(

    @Id
    @Column(name = "adapter_id", nullable = false, length = 500)
    var adapterId: String = "",

    @Column(name = "org_id", nullable = false)
    var orgId: String = "",

    @Column(name = "username", nullable = false)
    var username: String = "",

    @Column(name = "heartbeat_interval_in_minutes", nullable = false)
    var heartbeatIntervalInMinutes: Int = 0,

    @Column(name = "registered_time", nullable = false)
    var time: Long = 0,

    @Column(name = "last_heartbeat", nullable = false)
    var lastHeartbeat: Long = 0,

    @Column(name = "last_activity", nullable = false)
    var lastActivity: Long = 0,

    @Column(name = "has_contact", nullable = false)
    var hasContact: Boolean = true,

    @OneToMany(
        mappedBy = "contract",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    var capabilities: MutableSet<CapabilityEntity> = mutableSetOf()
) {


    fun updateLastActivity(newTime: Long) {
        if (newTime > lastActivity) {
            lastActivity = newTime
        }
    }

    fun getComponents(): Set<String> =
        capabilities.map { it.componentName }.toSet()

    fun getCapability(domain: String, pkg: String, resource: String): CapabilityEntity? =
        capabilities.firstOrNull {
            it.domainName.equals(domain, ignoreCase = true) &&
                    it.packageName.equals(pkg, ignoreCase = true) &&
                    it.resourceName.equals(resource, ignoreCase = true)
        }

    companion object {
        fun fromAdapterContract(adapterContract: AdapterContract): ContractEntity {
            val entity = ContractEntity(
                adapterId = adapterContract.adapterId,
                orgId = adapterContract.orgId,
                username = adapterContract.username,
                heartbeatIntervalInMinutes = adapterContract.heartbeatIntervalInMinutes,
                time = adapterContract.time,
                lastHeartbeat = 0,
                lastActivity = 0,
                hasContact = true
            )

            entity.capabilities = adapterContract.capabilities
                ?.map {
                    CapabilityEntity.fromAdapterCapability(
                        capability = it,
                        contract = entity
                    )
                }
                ?.toMutableSet()
                ?: mutableSetOf()

            return entity
        }
    }
}