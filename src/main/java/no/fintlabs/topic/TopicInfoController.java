package no.fintlabs.topic;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TopicInfoController {

    private final TopicInfoService topicInfoService;

    public TopicInfoController(TopicInfoService topicInfoService) {
        this.topicInfoService = topicInfoService;
    }

    @GetMapping("/topic/")
    public ResponseEntity<Map<String, Map<String, List<String>>>> getTopics() {
        return ResponseEntity.ok(topicInfoService.getCache());
    }

}
