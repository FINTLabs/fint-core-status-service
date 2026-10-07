package no.fintlabs.sync.kafka

class KafkaTopicConstants {
    companion object {
        val ADAPTER_FULL_SYNC = "adapter-full-sync"
        val ADAPTER_DELTA_SYNC = "adapter-delta-sync"
        val ADAPTER_DELETE_SYNC = "adapter-delete-sync"

        val ADAPTER_SYNC_TOPICS: Array<String> = arrayOf(
            ADAPTER_FULL_SYNC,
            ADAPTER_DELTA_SYNC,
            ADAPTER_DELETE_SYNC
        )
    }
}