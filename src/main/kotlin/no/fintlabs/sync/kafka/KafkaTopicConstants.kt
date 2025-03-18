package no.fintlabs.sync.kafka

class KafkaTopicConstants {
    companion object {
        val ADAPTER_SYNC_TOPICS: Array<String> = arrayOf(
            "adapter-full-sync",
            "adapter-delta-sync",
            "adapter-delete-sync"
        )
    }
}