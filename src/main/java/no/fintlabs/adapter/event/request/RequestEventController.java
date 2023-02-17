package no.fintlabs.adapter.event.request;

import no.fintlabs.adapter.entities.ConsumerRequestEntity;
import no.fintlabs.adapter.models.RequestFintEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("adapter/event/request")
@RestController
public class RequestEventController {

    private final RequestEventRepository requestEventRepository;

    public RequestEventController(RequestEventRepository requestEventRepository) {
        this.requestEventRepository = requestEventRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConsumerRequestEntity>> getRequests(@RequestBody(required = false) RequestFintEvent requestFintEvent) {
        if (requestFintEvent != null) {
            return ResponseEntity.ok(requestEventRepository.findRequestFintEventEntitiesByCorrId(requestFintEvent.getCorrId()));
        }
        return ResponseEntity.ok(requestEventRepository.findAll());
    }

}
