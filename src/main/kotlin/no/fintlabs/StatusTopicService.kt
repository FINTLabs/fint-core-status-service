package no.fintlabs

import no.fintlabs.kafka.common.topic.TopicNameParameters
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.kafka.event.topic.EventTopicService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class StatusTopicService(
    private val eventTopicService: EventTopicService
) {

    private val topicToRetensionMap: MutableMap<String, Long> = HashMap()
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun topicExists(topicNameParameters: TopicNameParameters): Boolean {
        return topicToRetensionMap.containsKey(topicNameParameters.topicName)
    }

    fun ensureTopic(topicNameParmeter: EventTopicNameParameters, retensionTime: Long) {
        log.info("Ensuring topic {} with retension time {}", topicNameParmeter.topicName, retensionTime)
        eventTopicService.ensureTopic(topicNameParmeter, retensionTime)
        topicToRetensionMap[topicNameParmeter.topicName] = retensionTime
    }

}