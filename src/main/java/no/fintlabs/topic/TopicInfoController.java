package no.fintlabs.topic;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/topic")
public class TopicInfoController {

    private final TopicInfoService topicInfoService;

    public TopicInfoController(TopicInfoService topicInfoService) {
        this.topicInfoService = topicInfoService;
    }

    @GetMapping("/getTopicInfo")
    public ResponseEntity<Map<String, Map<String, List<String>>>> getTopics() {
        log.info("This endpoint was reached");
        return ResponseEntity.ok(topicInfoService.getCache());
    }

}
