package no.novari.status.kafka

import no.novari.status.contract.ContractStore
import no.novari.status.event.EventStore
import no.novari.status.heartbeat.HeartbeatStore
import no.novari.status.sync.SyncStore
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import java.time.Instant

@Component
class StatusListener(
    private val jsonMapper: JsonMapper,
    private val contractStore: ContractStore,
    private val heartbeatStore: HeartbeatStore,
    private val syncStore: SyncStore,
    private val eventStore: EventStore,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [StatusTopics.CONTRACT])
    fun onContract(record: ConsumerRecord<String, String?>) {
        if (record.value() == null) {
            ContractKey.parse(record.key())?.let { contractStore.delete(it.username, it.orgId) }
                ?: logger.warn("Skipping contract tombstone without a valid key at offset {}", record.offset())
            return
        }
        handle(record) { contractStore.saveRegistration(jsonMapper.readValue<ContractMessage>(it), record.time()) }
    }

    @KafkaListener(topics = [StatusTopics.HEARTBEAT])
    fun onHeartbeat(record: ConsumerRecord<String, String?>) =
        handle(record) { heartbeatStore.save(jsonMapper.readValue<HeartbeatMessage>(it), record.time()) }

    @KafkaListener(topics = [StatusTopics.FULL_SYNC, StatusTopics.DELTA_SYNC, StatusTopics.DELETE_SYNC])
    fun onSyncPage(record: ConsumerRecord<String, String?>) =
        handle(record) {
            syncStore.savePage(StatusTopics.syncTypeOf(record.topic()), jsonMapper.readValue<SyncPageMessage>(it), record.time())
        }

    @KafkaListener(topics = [StatusTopics.EVENT_REQUEST])
    fun onRequest(record: ConsumerRecord<String, String?>) =
        handle(record) { eventStore.saveRequest(jsonMapper.readValue<RequestEventMessage>(it), record.time()) }

    @KafkaListener(topics = [StatusTopics.EVENT_RESPONSE])
    fun onResponse(record: ConsumerRecord<String, String?>) =
        handle(record) { eventStore.saveResponse(jsonMapper.readValue<ResponseEventMessage>(it), record.time()) }

    private fun handle(
        record: ConsumerRecord<String, String?>,
        save: (String) -> Unit,
    ) {
        val value = record.value() ?: return
        try {
            save(value)
        } catch (e: JacksonException) {
            logger.warn("Skipping unreadable record on {} at offset {}: {}", record.topic(), record.offset(), e.originalMessage)
        }
    }

    private fun ConsumerRecord<*, *>.time(): Instant = Instant.ofEpochMilli(timestamp())
}
