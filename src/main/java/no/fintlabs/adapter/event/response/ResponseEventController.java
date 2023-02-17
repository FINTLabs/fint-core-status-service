package no.fintlabs.adapter.event.response;

import no.fintlabs.adapter.entities.ConsumerResponseEntity;
import no.fintlabs.adapter.models.ResponseFintEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("adapter/event/response")
@RestController
public class ResponseEventController {

    private final ResponseEventRepository responseEventRepository;

    public ResponseEventController(ResponseEventRepository responseEventRepository) {
        this.responseEventRepository = responseEventRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConsumerResponseEntity>> getResponses(@RequestBody(required = false) ResponseFintEvent responseFintEvent) {
        if (responseFintEvent != null) {
            return ResponseEntity.ok(responseEventRepository.findResponseFintEventEntitiesByCorrId(responseFintEvent.getCorrId()));
        }
        return ResponseEntity.ok(responseEventRepository.findAll());
    }

}
